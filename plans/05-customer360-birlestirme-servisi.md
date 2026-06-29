# 05 — Customer360 Birleştirme Servisi

`Customer360Service`, dört connector'ı çağırıp tek bir `Customer360View` görünümü üretir.
Tüm toplam/özet hesapları **yalnızca burada** yapılır.

## Akış

```
getCustomer360(customerId):
  crm     = CrmConnector.fetchCustomer(id)        // yoksa CustomerNotFoundException
  orders  = OrderConnector.fetchOrders(id)        → OrderSummary
  billing = BillingConnector.fetchInvoices(id)    → BillingSummary
  support = SupportConnector.fetchTickets(id)     → SupportSummary
  → Customer360View (CRM profili + 3 özet)
```

## Özet Kuralları

| Özet | Hesap |
|------|-------|
| `OrderSummary.totalRevenue` | `CANCELLED` olmayan siparişlerin tutar toplamı |
| `OrderSummary.lastOrderNo` | en güncel `orderedAt`'a sahip siparişin numarası |
| `BillingSummary.outstandingDebt` | `paid=false` faturaların tutar toplamı (açık bakiye) |
| `BillingSummary.unpaidInvoices` | ödenmemiş fatura adedi |
| `SupportSummary.openTickets` | durumu `OPEN` veya `IN_PROGRESS` olan ticket adedi |

## DTO'lar (`aggregation.dto`, Java `record`)

- `Customer360View` — CRM profil alanları + `OrderSummary` + `BillingSummary` + `SupportSummary`
- `OrderSummary`, `BillingSummary`, `SupportSummary` — kaynak özetleri
- `CustomerListItem` — liste için hafif görünüm (id, ad, segment)

## Hata Yönetimi

Müşteri CRM'de yoksa `CustomerNotFoundException` (→ REST'te `404`, `@ResponseStatus` ile).

> Müşterinin hiç siparişi/faturası/ticket'ı olmasa bile özet bloğu **null değil**, sıfır
> değerlerle döner (örn. `outstandingDebt = 0`). Bu, REST/MCP tüketicileri için null-güvenliği sağlar.
