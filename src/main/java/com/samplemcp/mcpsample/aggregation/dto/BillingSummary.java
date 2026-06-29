package com.samplemcp.mcpsample.aggregation.dto;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * ERP/Faturalama kaynagindan turetilen ozet.
 *
 * @param totalInvoices    toplam fatura adedi
 * @param unpaidInvoices   odenmemis fatura adedi
 * @param outstandingDebt  acik bakiye (odenmemis faturalarin tutar toplami)
 */
@Builder
public record BillingSummary(
        int totalInvoices,
        int unpaidInvoices,
        BigDecimal outstandingDebt
) {
}
