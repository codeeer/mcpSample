# 03 — Veri Modeli ve H2 Seed

## H2 Konfigürasyonu (`application.properties`)

```properties
spring.datasource.url=jdbc:h2:mem:customer360;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.hibernate.ddl-auto=create-drop
```

- `DB_CLOSE_DELAY=-1`: bağlantı kapansa bile veri JVM süresince bellekte kalır.
- `create-drop`: tablolar açılışta entity'lerden üretilir, kapanışta silinir.
- Konsol: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:customer360`, kullanıcı `sa`).

## Entity'ler (her kaynak için bir tablo)

| Tablo | Entity | Önemli alanlar |
|-------|--------|----------------|
| `crm_customer` | `CrmCustomer` | `id` (= customerId), fullName, email, phone, city, segment, loyaltyTier, registeredAt |
| `order_record` | `OrderRecord` | customerId, orderNumber, amount, status (NEW/SHIPPED/DELIVERED/CANCELLED), orderedAt |
| `invoice` | `Invoice` | customerId, invoiceNumber, amount, paid, issuedAt, dueDate |
| `ticket` | `Ticket` | customerId, subject, priority, status (OPEN/IN_PROGRESS/CLOSED), createdAt |

`CrmCustomer.id` IDENTITY ile üretilir ve diğer tablolarda `customerId` olarak referans alınır
(yabancı anahtar kısıtı yok — kaynaklar gevşek bağlı kalsın diye bilinçli tercih).

## Rastgele Veri: `DataSeeder`

`CommandLineRunner` olarak açılışta çalışır:

1. **Datafaker** (`Locale("tr")`) ile **20 müşteri** üretir.
2. Her müşteriye rastgele sayıda:
   - **0–5 sipariş**, **0–4 fatura**, **0–3 ticket** bağlar.
3. Tutarlar `BigDecimal` (2 ondalık), durum/öncelik/segment sabit listelerden rastgele seçilir.
4. Sonunda özet log atar: `[SEED] ... musteri=20, siparis=..., fatura=..., ticket=...`

> Müşteri sayısını değiştirmek için `DataSeeder.CUSTOMER_COUNT` sabitini güncelleyin.
> Her müşterinin kayıt aralıklarını `run()` içindeki `rnd.nextInt(...)` değerleri belirler.
