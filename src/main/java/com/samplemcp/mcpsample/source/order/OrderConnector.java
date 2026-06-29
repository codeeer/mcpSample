package com.samplemcp.mcpsample.source.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Siparis yonetimi dis sistemini simule eden adaptor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConnector {

    private final OrderRepository orderRepository;

    /** Bir musterinin tum siparislerini getirir. */
    public List<OrderRecord> fetchOrders(Long customerId) {
        log.info("[ORDER] Siparisler cekiliyor: customerId={}", customerId);
        return orderRepository.findByCustomerId(customerId);
    }
}
