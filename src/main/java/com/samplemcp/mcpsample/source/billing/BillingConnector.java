package com.samplemcp.mcpsample.source.billing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ERP/Faturalama dis sistemini simule eden adaptor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingConnector {

    private final InvoiceRepository invoiceRepository;

    /** Bir musterinin tum faturalarini getirir. */
    public List<Invoice> fetchInvoices(Long customerId) {
        log.info("[BILLING] Faturalar cekiliyor: customerId={}", customerId);
        return invoiceRepository.findByCustomerId(customerId);
    }
}
