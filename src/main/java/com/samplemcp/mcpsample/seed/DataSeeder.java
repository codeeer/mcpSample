package com.samplemcp.mcpsample.seed;

import com.samplemcp.mcpsample.source.billing.Invoice;
import com.samplemcp.mcpsample.source.billing.InvoiceRepository;
import com.samplemcp.mcpsample.source.crm.CrmCustomer;
import com.samplemcp.mcpsample.source.crm.CrmRepository;
import com.samplemcp.mcpsample.source.order.OrderRecord;
import com.samplemcp.mcpsample.source.order.OrderRepository;
import com.samplemcp.mcpsample.source.support.Ticket;
import com.samplemcp.mcpsample.source.support.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Uygulama acilisinda H2'ye rastgele ornek veri yazar.
 * <p>
 * Her musteri icin rastgele sayida siparis, fatura ve destek talebi uretir;
 * boylece gercek dis sistemler olmadan Musteri 360 senaryosu calistirilabilir.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    /** Uretilecek musteri sayisi. */
    private static final int CUSTOMER_COUNT = 20;

    private static final String[] SEGMENTS = {"BIREYSEL", "KOBI", "KURUMSAL"};
    private static final String[] LOYALTY_TIERS = {"BRONZE", "SILVER", "GOLD", "PLATINUM"};
    private static final String[] ORDER_STATUSES = {"NEW", "SHIPPED", "DELIVERED", "CANCELLED"};
    private static final String[] TICKET_PRIORITIES = {"LOW", "MEDIUM", "HIGH", "URGENT"};
    private static final String[] TICKET_STATUSES = {"OPEN", "IN_PROGRESS", "CLOSED"};

    private final Faker faker = new Faker(new Locale("tr"));

    private final CrmRepository crmRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final TicketRepository ticketRepository;

    @Override
    public void run(String... args) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int orderTotal = 0, invoiceTotal = 0, ticketTotal = 0;

        for (int i = 0; i < CUSTOMER_COUNT; i++) {
            CrmCustomer customer = crmRepository.save(buildCustomer(rnd));
            Long customerId = customer.getId();

            List<OrderRecord> orders = new ArrayList<>();
            for (int o = 0; o < rnd.nextInt(0, 6); o++) {
                orders.add(buildOrder(customerId, rnd));
            }
            orderRepository.saveAll(orders);
            orderTotal += orders.size();

            List<Invoice> invoices = new ArrayList<>();
            for (int b = 0; b < rnd.nextInt(0, 5); b++) {
                invoices.add(buildInvoice(customerId, rnd));
            }
            invoiceRepository.saveAll(invoices);
            invoiceTotal += invoices.size();

            List<Ticket> tickets = new ArrayList<>();
            for (int t = 0; t < rnd.nextInt(0, 4); t++) {
                tickets.add(buildTicket(customerId, rnd));
            }
            ticketRepository.saveAll(tickets);
            ticketTotal += tickets.size();
        }

        log.info("[SEED] Rastgele veri uretildi -> musteri={}, siparis={}, fatura={}, ticket={}",
                CUSTOMER_COUNT, orderTotal, invoiceTotal, ticketTotal);
    }

    private CrmCustomer buildCustomer(ThreadLocalRandom rnd) {
        String name = faker.name().fullName();
        return CrmCustomer.builder()
                .fullName(name)
                .email(faker.internet().emailAddress())
                .phone(faker.phoneNumber().cellPhone())
                .city(faker.address().city())
                .segment(pick(SEGMENTS, rnd))
                .loyaltyTier(pick(LOYALTY_TIERS, rnd))
                .registeredAt(LocalDate.now().minusDays(rnd.nextInt(30, 1500)))
                .build();
    }

    private OrderRecord buildOrder(Long customerId, ThreadLocalRandom rnd) {
        return OrderRecord.builder()
                .customerId(customerId)
                .orderNumber("ORD-" + faker.number().digits(8))
                .amount(money(rnd, 50, 25_000))
                .status(pick(ORDER_STATUSES, rnd))
                .orderedAt(LocalDateTime.now().minusDays(rnd.nextInt(0, 365)))
                .build();
    }

    private Invoice buildInvoice(Long customerId, ThreadLocalRandom rnd) {
        LocalDate issued = LocalDate.now().minusDays(rnd.nextInt(0, 365));
        return Invoice.builder()
                .customerId(customerId)
                .invoiceNumber("INV-" + faker.number().digits(8))
                .amount(money(rnd, 100, 40_000))
                .paid(rnd.nextBoolean())
                .issuedAt(issued)
                .dueDate(issued.plusDays(30))
                .build();
    }

    private Ticket buildTicket(Long customerId, ThreadLocalRandom rnd) {
        return Ticket.builder()
                .customerId(customerId)
                .subject(faker.lorem().sentence(4))
                .priority(pick(TICKET_PRIORITIES, rnd))
                .status(pick(TICKET_STATUSES, rnd))
                .createdAt(LocalDateTime.now().minusDays(rnd.nextInt(0, 180)))
                .build();
    }

    private static String pick(String[] values, ThreadLocalRandom rnd) {
        return values[rnd.nextInt(values.length)];
    }

    private static BigDecimal money(ThreadLocalRandom rnd, int min, int max) {
        return BigDecimal.valueOf(rnd.nextDouble(min, max)).setScale(2, RoundingMode.HALF_UP);
    }
}
