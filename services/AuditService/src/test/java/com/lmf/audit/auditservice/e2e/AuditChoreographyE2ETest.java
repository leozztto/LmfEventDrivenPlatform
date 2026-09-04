package com.lmf.audit.auditservice.e2e;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lmf.audit.auditservice.Fixtures;
import com.lmf.audit.auditservice.domain.repository.AuditEventRepository;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.audit.auditservice.infrastructure.persistence.repository.SpringDataAuditEventRepository;
import com.lmf.audit.auditservice.integration.AbstractIntegrationTest;
import com.lmf.platform.contracts.EventMessage;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.contracts.FraudRejectedEvent;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.InventoryReservedEvent;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
import com.lmf.platform.messaging.InboxEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Ponta a ponta pelo broker real: publica nos sete tópicos da coreografia exatamente como
 * OrderService / FraudService / InventoryService / PaymentService fariam (JSON no tópico de
 * produção, sem type headers) e verifica o desfecho apenas pelo estado persistido do
 * AuditService — uma linha em {@code audit_events} por evento, com {@code topic}/{@code eventType}
 * corretos. Também exercita a idempotência de ponta a ponta (redelivery do mesmo eventId).
 *
 * <p>Precisa de um broker acessível (Docker/Linux). Roda no failsafe, no pacote {@code e2e}.
 */
class AuditChoreographyE2ETest extends AbstractIntegrationTest {

    private static final JsonMapper JSON = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .build();

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private SpringDataAuditEventRepository springDataAuditEventRepository;

    @Autowired
    private InboxEventRepository inboxEventRepository;

    @BeforeEach
    void setUp() {
        springDataAuditEventRepository.deleteAll();
        inboxEventRepository.deleteAll();
        kafkaListenerEndpointRegistry.start();
    }

    @AfterEach
    void stopListeners() {
        // Restaura o baseline "listeners desligados" para as classes de integração que rodam depois
        // e não esperam consumir do broker.
        kafkaListenerEndpointRegistry.stop();
    }

    @Test
    void recordsOneAuditRowPerEventAcrossAllSagaTopics() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        publish(KafkaTopics.ORDER_CREATED, orderId, Fixtures.orderCreated(orderId, customerId));
        publish(KafkaTopics.FRAUD_APPROVED, orderId, Fixtures.fraudApproved(orderId, customerId));
        publish(KafkaTopics.INVENTORY_RESERVED, orderId, Fixtures.inventoryReserved(orderId, customerId));
        publish(KafkaTopics.PAYMENT_APPROVED, orderId, Fixtures.paymentApproved(orderId, customerId));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(auditEventRepository.findByAggregateId(orderId))
                        .extracting(e -> e.getEventType())
                        .containsExactlyInAnyOrder(
                                OrderCreatedEvent.TYPE, FraudApprovedEvent.TYPE,
                                InventoryReservedEvent.TYPE, PaymentApprovedEvent.TYPE));
    }

    @Test
    void recordsFraudRejectedAndFailurePaths() {

        UUID fraudRejectedOrderId = UUID.randomUUID();
        UUID reservationFailedOrderId = UUID.randomUUID();
        UUID paymentFailedOrderId = UUID.randomUUID();

        publish(KafkaTopics.FRAUD_REJECTED, fraudRejectedOrderId, Fixtures.fraudRejected(fraudRejectedOrderId));
        publish(KafkaTopics.INVENTORY_RESERVATION_FAILED, reservationFailedOrderId, Fixtures.inventoryReservationFailed(reservationFailedOrderId));
        publish(KafkaTopics.PAYMENT_FAILED, paymentFailedOrderId, Fixtures.paymentFailed(paymentFailedOrderId, UUID.randomUUID()));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(auditEventRepository.findByAggregateId(fraudRejectedOrderId)).singleElement()
                    .satisfies(e -> assertThat(e.getEventType()).isEqualTo(FraudRejectedEvent.TYPE));
            assertThat(auditEventRepository.findByAggregateId(reservationFailedOrderId)).singleElement()
                    .satisfies(e -> assertThat(e.getEventType()).isEqualTo(InventoryReservationFailedEvent.TYPE));
            assertThat(auditEventRepository.findByAggregateId(paymentFailedOrderId)).singleElement()
                    .satisfies(e -> assertThat(e.getEventType()).isEqualTo(PaymentFailedEvent.TYPE));
        });
    }

    @Test
    void redeliveryOfTheSameEventDoesNotDuplicateTheAuditRow() {

        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, UUID.randomUUID());

        publish(KafkaTopics.ORDER_CREATED, orderId, event);
        publish(KafkaTopics.ORDER_CREATED, orderId, event);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(auditEventRepository.findByAggregateId(orderId)).isNotEmpty());

        // Dá tempo para uma eventual segunda entrega ser processada (e deduplicada).
        await().during(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(auditEventRepository.findByAggregateId(orderId)).hasSize(1));
    }

    private void publish(String topic, UUID orderId, EventMessage event) {
        try {
            kafkaTemplate.send(topic, orderId.toString(), JSON.writeValueAsString(event)).get();
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao publicar em " + topic, exception);
        }
    }
}
