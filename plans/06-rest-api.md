# 06 — REST API

`Customer360Controller` (`/api/customers`), birleşik 360 görünümünü ve kaynak bazlı ham
detayları sunar.

## Uçlar

| Metot & Yol | Açıklama | Döner |
|-------------|----------|-------|
| `GET /api/customers` | Tüm müşteriler | `CustomerListItem[]` (id, ad, segment) |
| `GET /api/customers/{id}/360` | Birleşik 360 görünümü | `Customer360View` |
| `GET /api/customers/{id}/orders` | Kaynak detayı: siparişler | `OrderRecord[]` |
| `GET /api/customers/{id}/invoices` | Kaynak detayı: faturalar | `Invoice[]` |
| `GET /api/customers/{id}/tickets` | Kaynak detayı: ticket'lar | `Ticket[]` |

## Örnek Çağrılar

```bash
curl localhost:8080/api/customers
curl localhost:8080/api/customers/1/360
curl localhost:8080/api/customers/1/invoices
```

## Örnek 360 Yanıtı (kısaltılmış)

```json
{
  "customerId": 1,
  "fullName": "Ahmet Yılmaz",
  "segment": "KURUMSAL",
  "loyaltyTier": "GOLD",
  "orders":  { "totalOrders": 3, "totalRevenue": 14250.00, "lastOrderNo": "ORD-48213907" },
  "billing": { "totalInvoices": 2, "unpaidInvoices": 1, "outstandingDebt": 8200.00 },
  "support": { "totalTickets": 2, "openTickets": 1 }
}
```

## Hatalar

- Olmayan müşteri → `404 Not Found` (`CustomerNotFoundException`).

> `360` ucu birleştirilmiş **özet** verir; ham kayıtlar için kaynak bazlı uçları (`/orders`,
> `/invoices`, `/tickets`) kullanın.
