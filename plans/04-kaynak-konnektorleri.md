# 04 — Kaynak Konnektörleri (Sahte Adaptör Katmanı)

## Desen

Her dış kaynak aynı üçlü desenle modellenir (`source.<kaynak>` paketinde):

```
Entity  →  JpaRepository  →  @Service Connector
```

- **Entity:** Kaynağa özgü JPA tablosu.
- **Repository:** `JpaRepository<Entity, Long>`; müşteri-bağlı kaynaklarda `findByCustomerId(...)`.
- **Connector:** "Dış sistem"i temsil eden `@Service`. Repository'den okur, çağrıyı loglar,
  veriyi entity olarak döner. Gerçek entegrasyona geçişte yalnızca bu sınıf değişir.

## Kaynaklar

| Kaynak | Connector | Sunduğu metot |
|--------|-----------|----------------|
| CRM | `CrmConnector` | `fetchAllCustomers()`, `fetchCustomer(id)` |
| Sipariş | `OrderConnector` | `fetchOrders(customerId)` |
| ERP/Fatura | `BillingConnector` | `fetchInvoices(customerId)` |
| Destek | `SupportConnector` | `fetchTickets(customerId)` |

## Örnek (CRM)

```java
@Slf4j @Service @RequiredArgsConstructor
public class CrmConnector {
    private final CrmRepository crmRepository;

    public List<CrmCustomer> fetchAllCustomers() {
        log.info("[CRM] Tum musteri profilleri cekiliyor...");
        return crmRepository.findAll();
    }
    public Optional<CrmCustomer> fetchCustomer(Long customerId) {
        log.info("[CRM] Musteri profili cekiliyor: customerId={}", customerId);
        return crmRepository.findById(customerId);
    }
}
```

## Yeni Kaynak Eklemek

1. `source.<yeni>` paketinde `Entity` + `JpaRepository` (gerekirse `findByCustomerId`) oluştur.
2. `<Yeni>Connector` `@Service` yaz (loglayıp repository'den oku).
3. `DataSeeder`'a rastgele üretim ekle.
4. `Customer360Service`'e özetini (bkz. [05](05-customer360-birlestirme-servisi.md)) ve gerekirse
   REST/MCP ucunu ekle.
