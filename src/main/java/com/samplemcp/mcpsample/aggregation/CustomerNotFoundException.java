package com.samplemcp.mcpsample.aggregation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * CRM'de istenen musteri bulunamadiginda firlatilir. REST katmaninda 404 doner.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Musteri bulunamadi: customerId=" + customerId);
    }
}
