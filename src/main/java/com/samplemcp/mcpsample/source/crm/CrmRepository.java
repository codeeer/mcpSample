package com.samplemcp.mcpsample.source.crm;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRM musteri tablosuna erisim. (Dahili — disariya connector uzerinden acilir.)
 */
public interface CrmRepository extends JpaRepository<CrmCustomer, Long> {
}
