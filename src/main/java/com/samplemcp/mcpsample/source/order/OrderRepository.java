package com.samplemcp.mcpsample.source.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Siparis tablosuna erisim.
 */
public interface OrderRepository extends JpaRepository<OrderRecord, Long> {

    List<OrderRecord> findByCustomerId(Long customerId);
}
