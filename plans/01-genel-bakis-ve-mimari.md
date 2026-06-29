# 01 — Genel Bakış ve Mimari

## Amaç

Gerçek dış sistemler (CRM, Sipariş Yönetimi, ERP/Faturalama, Destek/Ticket) yerine
**sahte adaptör katmanı** ile simüle edilen kaynaklardan veri çekip tek bir **Müşteri 360**
görünümünde birleştiren örnek bir uygulama. Gerçek DB bağlantısı yoktur; veriler açılışta
**H2 in-memory** veritabanına **rastgele** üretilir.

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

## Temel Tasarım İlkeleri

- **Kaynak izolasyonu:** Her dış sistem kendi paketinde (`source.crm`, `source.order`,
  `source.billing`, `source.support`) ve kendi H2 tablosunda. Birbirini doğrudan tanımaz.
- **Connector = sahte adaptör:** Gerçekte REST/SOAP çağrısı yapacakken H2'den okur,
  çağrıyı loglar. Böylece "dış sisteme gidiliyor" izlenimi korunur ve ileride gerçek
  entegrasyona geçiş tek noktada (connector) yapılır.
- **Ortak anahtar:** Tüm kaynaklar müşteriyi `customerId` (= `CrmCustomer.id`) ile bağlar.
- **Birleştirme tek yerde:** Toplam/özet hesapları yalnızca `Customer360Service` içinde.
- **Çift arayüz:** Aynı servis hem REST (`api`) hem MCP (`mcp`) üzerinden sunulur.

## İlgili Dokümanlar

- [02 — Build ve Bağımlılıklar](02-build-ve-bagimliliklar.md)
- [03 — Veri Modeli ve H2 Seed](03-veri-modeli-ve-h2-seed.md)
- [04 — Kaynak Konnektörleri](04-kaynak-konnektorleri.md)
- [05 — Customer360 Birleştirme Servisi](05-customer360-birlestirme-servisi.md)
- [06 — REST API](06-rest-api.md)
- [07 — MCP Server ve Araçlar](07-mcp-server-ve-araclar.md)
- [08 — Kurulum, Çalıştırma, Test](08-kurulum-calistirma-test.md)
