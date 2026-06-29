package com.samplemcp.mcpsample.source.support;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Destek sisteminden gelen tek bir destek talebi (ticket).
 */
@Entity
@Table(name = "ticket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ticket'in ait oldugu musteri (CRM customerId). */
    private Long customerId;

    private String subject;

    /** Oncelik: LOW, MEDIUM, HIGH, URGENT. */
    private String priority;

    /** Durum: OPEN, IN_PROGRESS, CLOSED. */
    private String status;

    private LocalDateTime createdAt;
}
