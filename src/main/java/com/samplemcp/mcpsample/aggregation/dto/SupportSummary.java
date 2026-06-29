package com.samplemcp.mcpsample.aggregation.dto;

import lombok.Builder;

/**
 * Destek/Ticket kaynagindan turetilen ozet.
 *
 * @param totalTickets  toplam ticket adedi
 * @param openTickets   acik (OPEN/IN_PROGRESS) ticket adedi
 */
@Builder
public record SupportSummary(
        int totalTickets,
        int openTickets
) {
}
