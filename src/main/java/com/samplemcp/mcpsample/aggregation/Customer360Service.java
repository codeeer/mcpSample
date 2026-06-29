package com.samplemcp.mcpsample.aggregation;

import com.samplemcp.mcpsample.aggregation.dto.BillingSummary;
import com.samplemcp.mcpsample.aggregation.dto.Customer360View;
import com.samplemcp.mcpsample.aggregation.dto.CustomerListItem;
import com.samplemcp.mcpsample.aggregation.dto.OrderSummary;
import com.samplemcp.mcpsample.aggregation.dto.SupportSummary;
import com.samplemcp.mcpsample.source.billing.BillingConnector;
import com.samplemcp.mcpsample.source.billing.Invoice;
import com.samplemcp.mcpsample.source.crm.CrmConnector;
import com.samplemcp.mcpsample.source.crm.CrmCustomer;
import com.samplemcp.mcpsample.source.order.OrderConnector;
import com.samplemcp.mcpsample.source.order.OrderRecord;
import com.samplemcp.mcpsample.source.support.SupportConnector;
import com.samplemcp.mcpsample.source.support.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Musteri 360'in cekirdegi: dort ayri kaynagi (CRM, siparis, fatura, destek)
 * cagirip tek bir {@link Customer360View} gorunumunde birlestirir.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Customer360Service {

    private static final Set<String> OPEN_TICKET_STATUSES = Set.of("OPEN", "IN_PROGRESS");
    private static final String CANCELLED_ORDER_STATUS = "CANCELLED";

    private final CrmConnector crmConnector;
    private final OrderConnector orderConnector;
    private final BillingConnector billingConnector;
    private final SupportConnector supportConnector;

    /** Tum musterilerin hafif listesini doner. */
    public List<CustomerListItem> listCustomers() {
        return crmConnector.fetchAllCustomers().stream()
                .map(c -> new CustomerListItem(c.getId(), c.getFullName(), c.getSegment()))
                .toList();
    }

    /**
     * Bir musteriyi tum kaynaklardan toplayip 360 gorunumu olusturur.
     *
     * @throws CustomerNotFoundException CRM'de musteri bulunamazsa
     */
    public Customer360View getCustomer360(Long customerId) {
        log.info("[360] Musteri birlestiriliyor: customerId={}", customerId);

        CrmCustomer crm = crmConnector.fetchCustomer(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        OrderSummary orders = summarizeOrders(orderConnector.fetchOrders(customerId));
        BillingSummary billing = summarizeBilling(billingConnector.fetchInvoices(customerId));
        SupportSummary support = summarizeSupport(supportConnector.fetchTickets(customerId));

        return Customer360View.builder()
                .customerId(crm.getId())
                .fullName(crm.getFullName())
                .email(crm.getEmail())
                .phone(crm.getPhone())
                .city(crm.getCity())
                .segment(crm.getSegment())
                .loyaltyTier(crm.getLoyaltyTier())
                .registeredAt(crm.getRegisteredAt())
                .orders(orders)
                .billing(billing)
                .support(support)
                .build();
    }

    private OrderSummary summarizeOrders(List<OrderRecord> orders) {
        BigDecimal revenue = orders.stream()
                .filter(o -> !CANCELLED_ORDER_STATUS.equals(o.getStatus()))
                .map(OrderRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String lastOrderNo = orders.stream()
                .max((a, b) -> a.getOrderedAt().compareTo(b.getOrderedAt()))
                .map(OrderRecord::getOrderNumber)
                .orElse(null);

        return OrderSummary.builder()
                .totalOrders(orders.size())
                .totalRevenue(revenue)
                .lastOrderNo(lastOrderNo)
                .build();
    }

    private BillingSummary summarizeBilling(List<Invoice> invoices) {
        List<Invoice> unpaid = invoices.stream().filter(i -> !i.isPaid()).toList();
        BigDecimal debt = unpaid.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BillingSummary.builder()
                .totalInvoices(invoices.size())
                .unpaidInvoices(unpaid.size())
                .outstandingDebt(debt)
                .build();
    }

    private SupportSummary summarizeSupport(List<Ticket> tickets) {
        long open = tickets.stream()
                .filter(t -> OPEN_TICKET_STATUSES.contains(t.getStatus()))
                .count();

        return SupportSummary.builder()
                .totalTickets(tickets.size())
                .openTickets((int) open)
                .build();
    }
}
