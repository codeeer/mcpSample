package com.samplemcp.mcpsample.source.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * CRM dis sistemini simule eden adaptor (connector).
 * <p>
 * Gercekte bir REST/SOAP CRM API'sine gidecekken, burada arkadaki H2 tablosundan
 * okur. Cagrilari loglayarak "dis sisteme gidildigi" izlenimini verir.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrmConnector {

    private final CrmRepository crmRepository;

    /** Tum musterileri getirir (dis CRM'den liste cekme). */
    public List<CrmCustomer> fetchAllCustomers() {
        log.info("[CRM] Tum musteri profilleri cekiliyor...");
        return crmRepository.findAll();
    }

    /** Tek bir musteri profilini getirir. */
    public Optional<CrmCustomer> fetchCustomer(Long customerId) {
        log.info("[CRM] Musteri profili cekiliyor: customerId={}", customerId);
        return crmRepository.findById(customerId);
    }
}
