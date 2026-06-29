package com.samplemcp.mcpsample.source.billing;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ERP/Faturalama sisteminden gelen tek bir fatura kaydi.
 */
@Entity
@Table(name = "invoice")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Faturanin ait oldugu musteri (CRM customerId). */
    private Long customerId;

    private String invoiceNumber;
    private BigDecimal amount;

    /** Fatura odendi mi? Odenmeyenler acik bakiyeyi olusturur. */
    private boolean paid;

    private LocalDate issuedAt;
    private LocalDate dueDate;
}
