package com.lmf.platform.contracts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que os contratos fazem round-trip JSON com nomes de campo estáveis — é o que os
 * produtores e consumidores dos serviços dependem na integração via Kafka.
 */
class ContractSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .build();

    @Test
    @DisplayName("OrderCreatedEvent faz round-trip")
    void orderCreatedRoundTrip() throws Exception {

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(), OrderCreatedEvent.TYPE, "v1", OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS),
                UUID.randomUUID(), "PENDING_PAYMENT", new BigDecimal("250.00"),
                new CustomerInfo(UUID.randomUUID(), "Ana", "ana@example.com", "11999999999"),
                new ShippingAddress("Rua A", "10", "São Paulo", "01000-000", "BR"),
                new PaymentInfo(PaymentMethod.PIX, 1, new BigDecimal("250.00")),
                List.of(new OrderItem(UUID.randomUUID(), 2, new BigDecimal("125.00"), new BigDecimal("250.00"))));

        String json = objectMapper.writeValueAsString(event);

        assertThat(json).contains("\"amount\":250.00").doesNotContain("paidAmount");

        OrderCreatedEvent back = objectMapper.readValue(json, OrderCreatedEvent.class);

        assertThat(back).isEqualTo(event);
    }

    @Test
    @DisplayName("InventoryReservedEvent faz round-trip com o payload de pagamento esperado")
    void inventoryReservedRoundTrip() throws Exception {

        InventoryReservedEvent event = new InventoryReservedEvent(
                UUID.randomUUID(), InventoryReservedEvent.TYPE, "v1", OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS),
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("99.90"),
                new PaymentInfo(PaymentMethod.CREDIT_CARD, 3, new BigDecimal("99.90")),
                List.of(new ReservedItem(UUID.randomUUID(), 1)));

        String json = objectMapper.writeValueAsString(event);

        InventoryReservedEvent back = objectMapper.readValue(json, InventoryReservedEvent.class);

        assertThat(back).isEqualTo(event);
        assertThat(back.payment().amount()).isEqualByComparingTo("99.90");
    }

    @Test
    @DisplayName("Eventos de pagamento fazem round-trip")
    void paymentEventsRoundTrip() throws Exception {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);

        PaymentApprovedEvent approved = new PaymentApprovedEvent(UUID.randomUUID(), PaymentApprovedEvent.TYPE, "v1", now,
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("10.00"), "BRL", PaymentMethod.PIX, "tx-1", "FAKE");

        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(approved), PaymentApprovedEvent.class)).isEqualTo(approved);

        PaymentFailedEvent failed = new PaymentFailedEvent(UUID.randomUUID(), PaymentFailedEvent.TYPE, "v1", now,
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("10.00"), "BRL", PaymentMethod.PIX, "declined", "FAILED");

        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(failed), PaymentFailedEvent.class)).isEqualTo(failed);
    }

    @Test
    @DisplayName("Eventos de fraude fazem round-trip")
    void fraudEventsRoundTrip() throws Exception {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);

        FraudApprovedEvent approved = new FraudApprovedEvent(
                UUID.randomUUID(), FraudApprovedEvent.TYPE, "v1", now,
                UUID.randomUUID(),
                new CustomerInfo(UUID.randomUUID(), "Ana", "ana@example.com", "11999999999"),
                new BigDecimal("250.00"),
                new PaymentInfo(PaymentMethod.PIX, 1, new BigDecimal("250.00")),
                List.of(new OrderItem(UUID.randomUUID(), 2, new BigDecimal("125.00"), new BigDecimal("250.00"))));

        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(approved), FraudApprovedEvent.class)).isEqualTo(approved);

        FraudRejectedEvent rejected = new FraudRejectedEvent(UUID.randomUUID(), FraudRejectedEvent.TYPE, "v1", now,
                UUID.randomUUID(), "Order amount exceeds limit");

        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(rejected), FraudRejectedEvent.class)).isEqualTo(rejected);
    }
}
