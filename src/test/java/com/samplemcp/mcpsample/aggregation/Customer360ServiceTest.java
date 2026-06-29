package com.samplemcp.mcpsample.aggregation;

import com.samplemcp.mcpsample.aggregation.dto.Customer360View;
import com.samplemcp.mcpsample.aggregation.dto.CustomerListItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Customer360Service} birlestirme mantigini, DataSeeder ile uretilen
 * rastgele veri uzerinde dogrular.
 */
@SpringBootTest
class Customer360ServiceTest {

    @Autowired
    private Customer360Service customer360Service;

    @Test
    void listCustomers_seederIleUretilenMusterileriDoner() {
        List<CustomerListItem> customers = customer360Service.listCustomers();
        assertThat(customers).isNotEmpty();
        assertThat(customers).allSatisfy(c -> {
            assertThat(c.customerId()).isNotNull();
            assertThat(c.fullName()).isNotBlank();
        });
    }

    @Test
    void getCustomer360_dortKaynaginOzetiniBirlestirir() {
        Long firstId = customer360Service.listCustomers().get(0).customerId();

        Customer360View view = customer360Service.getCustomer360(firstId);

        assertThat(view.customerId()).isEqualTo(firstId);
        assertThat(view.fullName()).isNotBlank();
        // Ozet bloklari her zaman dolu gelir (musterinin hic kaydi olmasa bile 0/null degil null-safe).
        assertThat(view.orders()).isNotNull();
        assertThat(view.billing()).isNotNull();
        assertThat(view.support()).isNotNull();
        assertThat(view.billing().outstandingDebt()).isNotNull();
    }

    @Test
    void getCustomer360_olmayanMusteriIcinHataFirlatir() {
        assertThatThrownBy(() -> customer360Service.getCustomer360(999_999L))
                .isInstanceOf(CustomerNotFoundException.class);
    }
}
