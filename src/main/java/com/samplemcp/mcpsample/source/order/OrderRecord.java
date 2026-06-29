package com.samplemcp.mcpsample.source.order;

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
import java.time.LocalDateTime;

/**
 * Siparis yonetimi sisteminden gelen tek bir siparis kaydi.
 */
@Entity
@Table(name = "order_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Siparisin ait oldugu musteri (CRM customerId). */
    private Long customerId;

    private String orderNumber;
    private BigDecimal amount;

    /** Siparis durumu: NEW, SHIPPED, DELIVERED, CANCELLED. */
    private String status;

    private LocalDateTime orderedAt;
}
