package com.samplemcp.mcpsample.api;

import com.samplemcp.mcpsample.aggregation.Customer360Service;
import com.samplemcp.mcpsample.aggregation.dto.Customer360View;
import com.samplemcp.mcpsample.aggregation.dto.CustomerListItem;
import com.samplemcp.mcpsample.source.billing.BillingConnector;
import com.samplemcp.mcpsample.source.billing.Invoice;
import com.samplemcp.mcpsample.source.order.OrderConnector;
import com.samplemcp.mcpsample.source.order.OrderRecord;
import com.samplemcp.mcpsample.source.support.SupportConnector;
import com.samplemcp.mcpsample.source.support.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Musteri 360 REST API'si.
 * <p>
 * Birlestirilmis 360 gorunumu ve kaynak bazli ham detaylar burada sunulur.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class Customer360Controller {

    private final Customer360Service customer360Service;
    private final OrderConnector orderConnector;
    private final BillingConnector billingConnector;
    private final SupportConnector supportConnector;

    /** Tum musteriler (id + ad + segment). */
    @GetMapping
    public List<CustomerListItem> listCustomers() {
        return customer360Service.listCustomers();
    }

    /** Birlestirilmis 360 gorunumu. */
    @GetMapping("/{id}/360")
    public Customer360View getCustomer360(@PathVariable Long id) {
        return customer360Service.getCustomer360(id);
    }

    /** Kaynak bazli detay: siparisler. */
    @GetMapping("/{id}/orders")
    public List<OrderRecord> getOrders(@PathVariable Long id) {
        return orderConnector.fetchOrders(id);
    }

    /** Kaynak bazli detay: faturalar. */
    @GetMapping("/{id}/invoices")
    public List<Invoice> getInvoices(@PathVariable Long id) {
        return billingConnector.fetchInvoices(id);
    }

    /** Kaynak bazli detay: destek talepleri. */
    @GetMapping("/{id}/tickets")
    public List<Ticket> getTickets(@PathVariable Long id) {
        return supportConnector.fetchTickets(id);
    }
}
