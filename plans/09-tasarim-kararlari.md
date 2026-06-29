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

## Transport neden iki tane? (Klasik HTTP+SSE + Streamable HTTP)

İki MCP HTTP transport'u vardır ve ikisini de destekliyoruz (aynı anda yalnızca biri açık):

| | **Klasik HTTP+SSE** | **Streamable HTTP** |
|---|---|---|
| MCP spec | 2024-11-05 | 2025-03-26 |
| Uç | `GET /sse` + `POST /mcp/message` (2 uç) | `POST`/`GET` `/mcp` (tek uç) |
| Akış | `/sse` SSE akışı açar, `endpoint` event'iyle mesaj URL'sini bildirir | `/mcp`'ye POST; cevap düz JSON **veya** SSE olabilir |
| Aktivasyon | varsayılan (property yok) | `spring.ai.mcp.server.protocol=STREAMABLE` (`streamable` profili) |

**Neden ikisi de var?** Klasik HTTP+SSE yaygın olsa da artık eskimekte; yeni ve önerilen
standart **Streamable HTTP** (tek uç, hem JSON hem SSE cevabı, Claude'un tercih ettiği). Bu
örnekte ikisini de bırakarak **farkı canlı karşılaştırılabilir** tutuyoruz:
- Varsayılan profil → klasik (`/sse` + `/mcp/message`).
- `streamable` profili → Streamable HTTP (`/mcp`); bu modda `/sse` 404 döner.

Aynı `spring-ai-starter-mcp-server-webmvc` starter'ı her ikisini de sağlar; seçim tek property
ile yapılır. İkisi de gerçek MCP client (Inspector) ile uçtan uca denenmiş ve çalışır
durumdadır — bkz. [07 — MCP Server ve Araçlar](07-mcp-server-ve-araclar.md#uçtan-uca-doğrulama-gerçek-mcp-client-ile).

### SSE nedir?

**SSE = Server-Sent Events** (Sunucu Tarafından Gönderilen Olaylar). HTTP üzerinde, sunucunun
**tek yönlü** olarak istemciye sürekli mesaj "push" edebildiği bir web standardı. Normal
HTTP'de: istemci sorar → sunucu bir kez cevaplar → bağlantı kapanır. SSE'de: bağlantı **açık
kalır**, sunucu istediği an veri gönderir (canlı akış/bildirim). Yön tek taraflıdır
(sunucu→istemci); istemci→sunucu için ayrı bir HTTP isteği gerekir.

### İki transport'un farkı (bağlantı modeli)

İkisi de aynı MCP mesajlarını taşır; fark **bağlantıyı nasıl kurdukları**:

**Klasik HTTP+SSE — "2 kanal":**
```
İstemci ── GET /sse ─────────────►  Sunucu
        ◄═══ SSE akışı (hep açık) ══   ← cevaplar buradan akar
İstemci ── POST /mcp/message ─────►  Sunucu   ← komutlar buradan gider
```
- İki ayrı uç: komut için `POST /mcp/message`, cevaplar için sürekli açık `GET /sse`.
- Sunucu her istemci için **açık SSE bağlantısını tutmak zorunda** (stateful).

**Streamable HTTP — "tek kanal, esnek":**
```
İstemci ── POST /mcp ─────────────►  Sunucu
        ◄── cevap: ya düz JSON ya da (gerekirse) SSE akışına yükseltilir
```
- Tek uç (`/mcp`). Kısa cevap → düz JSON; uzun/çoklu mesaj gerekiyorsa **o anda** SSE'ye geçer.
  ("Streamable" adı buradan: SSE *gerektiğinde* devreye girer, zorunlu değil.)
- Sürekli açık bağlantı şart değil → **stateless** çalışabilir, ölçekleme ve proxy/load-balancer
  arkasında çalışma daha kolay.

| | Klasik HTTP+SSE | Streamable HTTP |
|---|---|---|
| Uç sayısı | 2 (`/sse` + `/mcp/message`) | 1 (`/mcp`) |
| Sunucu→istemci | **Hep açık** SSE akışı | Cevap **JSON veya gerektiğinde** SSE |
| Bağlantı durumu | Açık tutulmalı (stateful) | Açık tutmak şart değil (stateless olabilir) |
| Ölçekleme | Zor (her istemci açık bağlantı) | Kolay |
| Durum | Eskiyen | Güncel/önerilen |

> Benzetme: **klasik** = "sürekli açık telefon hattı + ayrı mektup kutusu"; **streamable** =
> "tek numarayı ararsın; kısa cevapsa hemen söyler, uzun cevapsa hattı açık tutup anlatır".

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
