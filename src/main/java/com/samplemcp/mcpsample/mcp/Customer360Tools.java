package com.samplemcp.mcpsample.mcp;

import com.samplemcp.mcpsample.aggregation.Customer360Service;
import com.samplemcp.mcpsample.aggregation.dto.BillingSummary;
import com.samplemcp.mcpsample.aggregation.dto.Customer360View;
import com.samplemcp.mcpsample.aggregation.dto.CustomerListItem;
import com.samplemcp.mcpsample.aggregation.dto.SupportSummary;
import com.samplemcp.mcpsample.source.order.OrderConnector;
import com.samplemcp.mcpsample.source.order.OrderRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP araclari: bir LLM ajaninin musteri 360 verisini sorgulamasini saglar.
 * <p>
 * Her metot {@link Tool} ile isaretlenmistir; aciklamalar (description) LLM'in
 * dogru araci secebilmesi icin Turkce yazilmistir. Araclar
 * {@code McpToolConfig} icindeki {@code ToolCallbackProvider} ile MCP server'a
 * kaydedilir.
 */
@Component
@RequiredArgsConstructor
public class Customer360Tools {

    private final Customer360Service customer360Service;
    private final OrderConnector orderConnector;

    @Tool(description = "Sistemdeki tum musterileri (id, ad, segment) listeler.")
    public List<CustomerListItem> listCustomers() {
        return customer360Service.listCustomers();
    }

    @Tool(description = "Verilen musteri icin CRM, siparis, fatura ve destek verisini "
            + "birlestirilmis tek bir 360 gorunumu olarak doner.")
    public Customer360View getCustomer360(
            @ToolParam(description = "Musteri kimligi (customerId)") Long customerId) {
        return customer360Service.getCustomer360(customerId);
    }

    @Tool(description = "Verilen musterinin tum siparis kayitlarini doner.")
    public List<OrderRecord> getCustomerOrders(
            @ToolParam(description = "Musteri kimligi (customerId)") Long customerId) {
        return orderConnector.fetchOrders(customerId);
    }

    @Tool(description = "Verilen musterinin acik bakiyesini (odenmemis fatura toplami) "
            + "ve fatura ozetini doner.")
    public BillingSummary getOutstandingBalance(
            @ToolParam(description = "Musteri kimligi (customerId)") Long customerId) {
        return customer360Service.getCustomer360(customerId).billing();
    }

    @Tool(description = "Verilen musterinin acik destek talebi (ticket) ozetini doner.")
    public SupportSummary getOpenTickets(
            @ToolParam(description = "Musteri kimligi (customerId)") Long customerId) {
        return customer360Service.getCustomer360(customerId).support();
    }
}
