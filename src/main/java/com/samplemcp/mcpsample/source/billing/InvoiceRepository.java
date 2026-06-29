package com.samplemcp.mcpsample.source.billing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Fatura tablosuna erisim.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByCustomerId(Long customerId);
}
