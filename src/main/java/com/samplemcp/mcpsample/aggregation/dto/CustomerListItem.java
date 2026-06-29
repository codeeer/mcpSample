package com.samplemcp.mcpsample.aggregation.dto;

/**
 * Musteri listesi icin hafif gorunum (id + ad).
 */
public record CustomerListItem(
        Long customerId,
        String fullName,
        String segment
) {
}
