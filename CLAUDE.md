# CLAUDE.md

Bu dosya, bu depoda çalışırken Claude Code'a (claude.ai/code) rehberlik eder.

## Proje Nedir?

**Müşteri 360** örnek uygulaması. Gerçek dış sistemler (CRM, Sipariş Yönetimi,
ERP/Faturalama, Destek/Ticket) yerine **sahte adaptör (connector) katmanı** ile simüle
edilen kaynaklardan veri çekip tek bir **birleşik müşteri görünümünde** birleştirir.

Gerçek bir veritabanı bağlantısı **yoktur**: tüm veri, uygulama açılışında **H2 in-memory**
veritabanına `DataSeeder` tarafından **rastgele** üretilerek yazılır.

Veri dışarıya iki şekilde sunulur:
- **REST API** (`/api/customers/...`)
- **MCP Server** (Spring AI) — bir LLM ajanı `@Tool` araçlarıyla veriyi sorgulayabilir.

## Teknoloji

- Spring Boot **4.1.0**, **Java 21** (Spring AI 2.0 gereği), WAR paketleme
- Spring Data JPA + **H2 (in-memory)**
- Spring AI **2.0.0** — `spring-ai-starter-mcp-server-webmvc` (SSE transport)
- **Datafaker** (rastgele örnek veri)
- Lombok

## Mimari

```
İstemci (REST / LLM-MCP)
        │
  Customer360Controller        Customer360Tools (@Tool)
        └───────────┬───────────────┘
                    ▼
          Customer360Service           ← 4 kaynağı çağırıp birleştirir
                    │
   ┌────────┬───────┴────────┬──────────────┐
CrmConnector OrderConnector BillingConnector SupportConnector   ← sahte dış sistem adaptörleri
   └────────┴───────┬────────┴──────────────┘
                    ▼
            JPA Repository → H2 (DataSeeder ile seed)
```

**Tasarım fikri:** Her `*Connector` "dış API çağrısı" gibi davranır (çağrıyı loglar), ama
arkada H2'deki kendi tablosundan okur. Tüm kaynaklar müşteriyi ortak `customerId` ile bağlar
(bu, `CrmCustomer.id` değeridir).

## Paket Rehberi (`com.samplemcp.mcpsample`)

| Paket | Sorumluluk |
|-------|-----------|
| `source.crm` / `source.order` / `source.billing` / `source.support` | Her dış kaynak için `Entity` + `Repository` + `Connector` (sahte adaptör) |
| `aggregation` | `Customer360Service` (birleştirme) + `dto/` (görünüm/özet record'ları) |
| `api` | `Customer360Controller` — REST uçları |
| `mcp` | `Customer360Tools` — `@Tool` ile MCP araçları |
| `config` | `McpToolConfig` — `ToolCallbackProvider` bean'i |
| `seed` | `DataSeeder` — açılışta rastgele veri üretir |

> Yeni bir kaynak eklerken `source.crm` desenini izleyin: `Entity` → `JpaRepository`
> (`findByCustomerId`) → `@Service Connector`, sonra `Customer360Service`'e özetini ekleyin.

## Çalıştırma & Test

> **Java 21 gerekir.** Homebrew ile kurulduysa `java_home -v 21` görmeyebilir; `JAVA_HOME`'u
> doğrudan ayarlayın:
> `export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"`

```bash
./mvnw spring-boot:run     # uygulamayı başlat (açılışta seed log'u görünür)
./mvnw clean test          # testleri çalıştır
./mvnw clean package        # WAR üret
```

### Uçlar
- REST: `GET /api/customers`, `GET /api/customers/{id}/360`,
  `.../{id}/orders|invoices|tickets`
- H2 konsolu: `http://localhost:8080/h2-console` — JDBC URL: `jdbc:h2:mem:customer360`,
  kullanıcı `sa`, şifre boş
- MCP SSE ucu: `http://localhost:8080/sse` (mesaj ucu: `/mcp/message`)

### Hızlı kontrol
```bash
curl localhost:8080/api/customers
curl localhost:8080/api/customers/1/360
```

### MCP'yi uçtan uca doğrulama (gerçek client)
```bash
npx -y @modelcontextprotocol/inspector --cli http://localhost:8080/sse \
  --transport sse --method tools/list
npx -y @modelcontextprotocol/inspector --cli http://localhost:8080/sse \
  --transport sse --method tools/call --tool-name getCustomer360 --tool-arg customerId=1
```
Detaylar: [plans/07-mcp-server-ve-araclar.md](plans/07-mcp-server-ve-araclar.md#uçtan-uca-doğrulama-gerçek-mcp-client-ile)

## Konvansiyonlar

- **Doküman ve yorumlar Türkçe**, sınıf/değişken adları İngilizce.
- DTO'lar Java `record` (Lombok `@Builder` ile); entity'ler Lombok `@Data/@Builder`.
- Connector'lar veriyi entity olarak döner; özetleme (toplam/açık bakiye vb.) yalnızca
  `Customer360Service` içinde yapılır.
- Detaylı tasarım notları `plans/` klasöründedir (`01-...` → `08-...`).
