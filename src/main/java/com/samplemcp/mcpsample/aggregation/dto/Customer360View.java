package com.samplemcp.mcpsample.aggregation.dto;

import lombok.Builder;

import java.time.LocalDate;

/**
 * Dort ayri kaynaktan (CRM, siparis, ERP/fatura, destek) birlestirilmis
 * tek bir musteri 360 gorunumu.
 */
@Builder
public record Customer360View(
        // --- CRM profil bilgileri ---
        Long customerId,
        String fullName,
        String email,
        String phone,
        String city,
        String segment,
        String loyaltyTier,
        LocalDate registeredAt,
        // --- Kaynak ozetleri ---
        OrderSummary orders,
        BillingSummary billing,
        SupportSummary support
) {
}
