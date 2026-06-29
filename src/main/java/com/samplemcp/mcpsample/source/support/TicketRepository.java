package com.samplemcp.mcpsample.source.support;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Destek talebi (ticket) tablosuna erisim.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCustomerId(Long customerId);
}
