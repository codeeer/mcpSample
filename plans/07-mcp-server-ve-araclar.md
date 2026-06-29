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

## Transport: iki seçenek

Proje **iki MCP transport'unu** da destekler. Aynı anda yalnızca biri çalışır; seçim
`spring.ai.mcp.server.protocol` ile yapılır (aynı `spring-ai-starter-mcp-server-webmvc` starter).

| | **Klasik HTTP+SSE** (varsayılan) | **Streamable HTTP** (`streamable` profili) |
|---|---|---|
| MCP spec | 2024-11-05 | 2025-03-26 (güncel, Claude'un tercih ettiği) |
| Aktifleştirme | varsayılan (property yok) | `spring.ai.mcp.server.protocol=STREAMABLE` |
| Uç sayısı | **2 uç** | **tek uç** |
| Uçlar | `GET /sse` (akış) + `POST /mcp/message` | `POST`/`GET` `/mcp` |
| Nasıl | `/sse` SSE akışı açar, server `endpoint` event'iyle mesaj URL'sini verir; client oraya POST eder | Client `/mcp`'ye POST eder; cevap düz JSON **veya** SSE akışı olabilir ("streamable") |
| Inspector transport | `--transport sse` | `--transport http` |

> **Neden iki tane?** Klasik HTTP+SSE yaygın ama artık eskimekte; yeni standart Streamable
> HTTP. İkisini de bırakarak farkı canlı görebilirsiniz. Karar gerekçesi:
> [09 — Tasarım Kararları](09-tasarim-kararlari.md#transport-neden-iki-tane-klasik-httpsse--streamable-http).

### Klasik HTTP+SSE (varsayılan profil)
```bash
./mvnw spring-boot:run
# GET  http://localhost:8080/sse           → event:endpoint /mcp/message?sessionId=...
# POST http://localhost:8080/mcp/message
```

### Streamable HTTP (`streamable` profili)
`src/main/resources/application-streamable.properties` ile açılır:
```properties
spring.ai.mcp.server.protocol=STREAMABLE
spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp
```
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=streamable
# Tek uç: http://localhost:8080/mcp   (bu profilde /sse KAPALI → 404)
```

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

`claude_desktop_config.json` benzeri bir istemcide aktif transport'a göre ucu tanımlayın:

```json
{
  "mcpServers": {
    "customer360-sse":        { "url": "http://localhost:8080/sse" },
    "customer360-streamable": { "url": "http://localhost:8080/mcp" }
  }
}
```

> Hangi uç çalışıyorsa (varsayılan profil → `/sse`, `streamable` profili → `/mcp`) ona karşılık
> gelen kaydı kullanın; ikisi aynı anda açık değildir.

> Yeni araç eklemek için `Customer360Tools`'a `@Tool` metodu ekleyin — ayrıca kayıt gerekmez,
> aynı bean üzerinden otomatik yayınlanır.

## Uçtan Uca Doğrulama (gerçek MCP client ile)

Aşağıdaki adımlar, araçların **MCP protokolü üzerinden** gerçekten çağrılabildiğini kanıtlar
(unit testler protokolü kullanmaz). Resmi **MCP Inspector** bir MCP client olarak bağlanır.
Her iki transport için de denenmiş ve çalışır durumdadır.

Önce Java 21'i ayarlayın: `export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"`.
Açılış logunda her iki modda da `Registered tools: 5` görünür.

### A) Klasik HTTP+SSE (varsayılan)
```bash
./mvnw spring-boot:run                              # ayrı terminalde

# handshake — endpoint event'i döner:
curl -s -N --max-time 2 http://localhost:8080/sse
# event:endpoint  data:/mcp/message?sessionId=...

npx -y @modelcontextprotocol/inspector --cli http://localhost:8080/sse \
  --transport sse --method tools/list
npx -y @modelcontextprotocol/inspector --cli http://localhost:8080/sse \
  --transport sse --method tools/call --tool-name getCustomer360 --tool-arg customerId=1
```

### B) Streamable HTTP (`streamable` profili)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=streamable   # ayrı terminalde

curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/sse   # 404 (klasik kapalı)

npx -y @modelcontextprotocol/inspector --cli http://localhost:8080/mcp \
  --transport http --method tools/list
npx -y @modelcontextprotocol/inspector --cli http://localhost:8080/mcp \
  --transport http --method tools/call --tool-name getCustomer360 --tool-arg customerId=1
```

İki modda da beklenen: `content[].text` içinde birleşik 360 JSON'u, `isError: false`. Aynı veri
REST `GET /api/customers/1/360` ile birebir eşleşir.

> Görsel arayüz için argümansız `npx @modelcontextprotocol/inspector` çalıştırıp tarayıcıda
> transport (**SSE** → `/sse` veya **Streamable HTTP** → `/mcp`) ve URL'yi girin.
