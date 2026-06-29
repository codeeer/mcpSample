package com.samplemcp.mcpsample.source.crm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * CRM sisteminden gelen musteri profili.
 * <p>
 * Bu entity'nin {@code id} alani, tum kaynaklar arasinda musteriyi baglayan
 * ortak {@code customerId} olarak kullanilir.
 */
@Entity
@Table(name = "crm_customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String phone;
    private String city;

    /** Musteri segmenti: BIREYSEL, KOBI, KURUMSAL. */
    private String segment;

    /** Sadakat seviyesi: BRONZE, SILVER, GOLD, PLATINUM. */
    private String loyaltyTier;

    private LocalDate registeredAt;
}
