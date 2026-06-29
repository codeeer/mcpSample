# 09 — Tasarım Kararları ve Gerekçeleri

Bu dosya, projedeki "neden böyle yaptık?" sorularının cevaplarını toplar. Yeni bir
tartışmalı/öğretici karar verildiğinde buraya kısa bir madde ekleyin.

## MCP server tipi neden `SYNC`?

`application.properties` → `spring.ai.mcp.server.type=SYNC`

Spring AI MCP server iki çalışma modeli sunar:

| Mod | Programlama modeli | Web yığını | Veri erişimi |
|-----|--------------------|-----------|--------------|
| **`SYNC`** | Bloklayan (imperative); `@Tool` düz nesne döner | Spring **WebMVC** (servlet) | JPA/JDBC (bloklayan) |
| **`ASYNC`** | Reaktif; `Mono`/`Flux` | Spring **WebFlux** | R2DBC vb. (reaktif) |

**Kararımız: `SYNC`.** Çünkü tüm yığınımız bloklayan:
- Starter: `spring-ai-starter-mcp-server-webmvc` (servlet/WebMVC).
- Veri katmanı: JPA + Hibernate (bloklayan çağrılar).
- Connector/servisler: düz, bloklayan metotlar.

`ASYNC` seçilseydi hem WebMVC servlet yığınıyla hem de bloklayan JPA çağrılarıyla uyumsuz
olurdu (reaktif zincir içinde bloklama = anti-pattern). Reaktif istenseydi
`...-mcp-server-webflux` + `ASYNC` + R2DBC gerekirdi. Bu örnek proje için `SYNC` hem daha
basit hem de mevcut mimariyle tutarlı.

## Java neden 21? (17 değil)

Spring Boot 4'ün tabanı Java 17 olmasına rağmen, MCP için kullandığımız **Spring AI 2.0**
derleme için **Java 21** gerektiriyor. Detay: [02 — Build ve Bağımlılıklar](02-build-ve-bagimliliklar.md).

## Transport neden HTTP+SSE? (streamable-http değil)

`spring-ai-starter-mcp-server-webmvc` varsayılan olarak **SSE** transport'u açar:
`GET /sse` → `event:endpoint /mcp/message?sessionId=...`. `/mcp` (streamable-http) bu kurulumda
404 döner. SSE, MCP Inspector ve Claude Desktop gibi yaygın client'larla doğrudan çalışır.
Doğrulama: [07 — MCP Server ve Araçlar](07-mcp-server-ve-araclar.md#uçtan-uca-doğrulama-gerçek-mcp-client-ile).

## Araçlar neden `ToolCallbackProvider` ile kaydediliyor?

`Customer360Tools` `@Tool` metotlarını içerir; `McpToolConfig` bunları bir
`MethodToolCallbackProvider` bean'ine sarar. Spring AI MCP starter, context'teki
`ToolCallbackProvider` bean'lerini otomatik yayınlar (açılış logunda `Registered tools: 5`).
Bu, araç tanımını (ne yaptığı) kayıt mekanizmasından (nasıl yayınlandığı) ayırır.

> Not: Açılışta `SyncMcpToolProvider: No tool methods found ... []` WARN'ı görülebilir; bu,
> kullanmadığımız **annotation-scanner** yolundan gelir. Bizim `ToolCallbackProvider` yolumuz
> araçları başarıyla kaydeder (`Registered tools: 5`), dolayısıyla bu uyarı zararsızdır.

## Kaynaklar neden gevşek bağlı? (FK yok)

`OrderRecord`, `Invoice`, `Ticket` müşteriyi `customerId` (düz `Long`) ile referanslar; JPA
ilişkisi/yabancı anahtar kısıtı **bilinçli olarak yok**. Amaç, her kaynağın gerçek hayatta
ayrı bir dış sistem olduğunu modellemek — aralarında DB seviyesinde bağ kurmamak. Birleştirme
yalnızca `Customer360Service` içinde, kod seviyesinde yapılır.

## Özetler neden null yerine sıfır döner?

Müşterinin hiç siparişi/faturası/ticket'ı olmasa bile özet bloğu (`OrderSummary` vb.) **null
değil**, sıfır değerlerle döner. Böylece REST/MCP tüketicileri (özellikle LLM'ler) null kontrolü
yapmak zorunda kalmaz. Detay: [05 — Customer360 Birleştirme Servisi](05-customer360-birlestirme-servisi.md).

## H2 neden `create-drop` + in-memory?

Örnek/demo amaçlı: her açılışta temiz şema + `DataSeeder` ile rastgele veri. Kalıcılık
hedeflenmez. Gerçek senaryoda connector'lar gerçek dış sistemlere bağlanır ve bu katman kalkar.
Detay: [03 — Veri Modeli ve H2 Seed](03-veri-modeli-ve-h2-seed.md).
