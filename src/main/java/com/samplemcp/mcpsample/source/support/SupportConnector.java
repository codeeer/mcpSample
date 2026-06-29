package com.samplemcp.mcpsample.source.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Destek/Ticket dis sistemini simule eden adaptor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupportConnector {

    private final TicketRepository ticketRepository;

    /** Bir musterinin tum destek taleplerini getirir. */
    public List<Ticket> fetchTickets(Long customerId) {
        log.info("[SUPPORT] Ticket'lar cekiliyor: customerId={}", customerId);
        return ticketRepository.findByCustomerId(customerId);
    }
}
