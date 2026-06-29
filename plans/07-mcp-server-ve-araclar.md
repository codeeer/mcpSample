# 07 — MCP Server ve Araçlar

Proje, Spring AI MCP Server starter'ı ile bir **MCP sunucusu** olarak da davranır; böylece
bir LLM ajanı (örn. Claude) Müşteri 360 verisini **araçlar (tools)** üzerinden sorgular.

## Konfigürasyon (`application.properties`)

```properties
spring.ai.mcp.server.name=customer360-mcp-server
spring.ai.mcp.server.version=0.0.1
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.instructions=Musteri 360 verisine ... erisim saglayan araclar.
```

Transport: **WebMVC/SSE**.
- SSE ucu: `http://localhost:8080/sse`
- Mesaj ucu: `http://localhost:8080/mcp/message`

## Araçların Tanımı (`mcp.Customer360Tools`)

Her metot `@Tool(description=...)` ile işaretlidir; açıklamalar LLM'in doğru aracı seçmesi
için Türkçe yazılmıştır. Parametreler `@ToolParam` ile açıklanır.

| Araç | İş |
|------|----|
| `listCustomers()` | Tüm müşterileri (id, ad, segment) listeler |
| `getCustomer360(customerId)` | Birleşik 360 görünümü döner |
| `getCustomerOrders(customerId)` | Müşterinin tüm siparişleri |
| `getOutstandingBalance(customerId)` | Açık bakiye + fatura özeti (`BillingSummary`) |
| `getOpenTickets(customerId)` | Açık destek talebi özeti (`SupportSummary`) |

## Kayıt (`config.McpToolConfig`)

`@Tool` metotları, bir `ToolCallbackProvider` bean'i ile MCP server'a kaydedilir:

```java
@Bean
public ToolCallbackProvider customer360ToolCallbackProvider(Customer360Tools tools) {
    return MethodToolCallbackProvider.builder().toolObjects(tools).build();
}
```

Spring AI MCP starter, context'teki `ToolCallbackProvider` bean'lerini otomatik yayınlar.

## Bir MCP İstemcisine Bağlama

`claude_desktop_config.json` benzeri bir istemcide SSE ucunu tanımlayın:

```json
{
  "mcpServers": {
    "customer360": { "url": "http://localhost:8080/sse" }
  }
}
```

> Yeni araç eklemek için `Customer360Tools`'a `@Tool` metodu ekleyin — ayrıca kayıt gerekmez,
> aynı bean üzerinden otomatik yayınlanır.

## Uçtan Uca Doğrulama (gerçek MCP client ile)

Aşağıdaki adımlar, araçların **MCP protokolü üzerinden** gerçekten çağrılabildiğini kanıtlar
(unit testler protokolü kullanmaz). Resmi **MCP Inspector** bir MCP client olarak bağlanır.

1. Uygulamayı başlatın (ayrı bir terminalde):
   ```bash
   export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
   ./mvnw spring-boot:run
   ```
   Açılış logunda `Registered tools: 5` görünür.

2. SSE ucunu doğrulayın (handshake — endpoint event'i döner):
   ```bash
   curl -s -N --max-time 2 http://localhost:8080/sse
   # event:endpoint
   # data:/mcp/message?sessionId=...
   ```

3. Araçları listeleyin (`tools/list`):
   ```bash
   npx -y @modelcontextprotocol/inspector --cli http://localhost:8080/sse \
     --transport sse --method tools/list
   ```

4. Bir aracı çağırın (`tools/call`):
   ```bash
   npx -y @modelcontextprotocol/inspector --cli http://localhost:8080/sse \
     --transport sse --method tools/call --tool-name getCustomer360 --tool-arg customerId=1
   ```
   Beklenen: `content[].text` içinde birleşik 360 JSON'u, `isError: false`. Aynı veri REST
   `GET /api/customers/1/360` ile birebir eşleşir.

> Görsel arayüz için argümansız `npx @modelcontextprotocol/inspector` çalıştırıp tarayıcıda
> transport olarak **SSE** ve URL olarak `http://localhost:8080/sse` girin.
