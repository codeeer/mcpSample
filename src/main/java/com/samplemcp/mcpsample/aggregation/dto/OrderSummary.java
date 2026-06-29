package com.samplemcp.mcpsample.aggregation.dto;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Siparis yonetimi kaynagindan turetilen ozet.
 *
 * @param totalOrders   toplam siparis adedi
 * @param totalRevenue  toplam ciro (iptal edilmeyen siparislerin tutar toplami)
 * @param lastOrderNo   en son siparis numarasi (yoksa null)
 */
@Builder
public record OrderSummary(
        int totalOrders,
        BigDecimal totalRevenue,
        String lastOrderNo
) {
}
