package com.lmf.audit.auditservice.integration;

import com.lmf.audit.auditservice.Fixtures;
import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.domain.repository.AuditEventRepository;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.audit.auditservice.infrastructure.kafka.consumer.OrderCreatedConsumer;
import com.lmf.audit.auditservice.infrastructure.persistence.repository.SpringDataAuditEventRepository;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.contracts.FraudRejectedEvent;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.InventoryReservedEvent;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
import com.lmf.platform.messaging.InboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fluxo completo do AuditService a partir do use case (sem depender do broker real): grava o
 * envelope de cada um dos sete tópicos consumidos e dedupe via Inbox.
 */
class AuditFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RecordAuditEventUseCase recordAuditEventUseCase;

    @Autowired
    private OrderCreatedConsumer orderCreatedConsumer;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private SpringDataAuditEventRepository springDataAuditEventRepository;

    @Autowired
    private InboxEventRepository inboxEventRepository;

    @BeforeEach
    void clean() {
        springDataAuditEventRepository.deleteAll();
        inboxEventRepository.deleteAll();
    }

    @Test
    void recordsOrderCreatedEvent() {

        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, UUID.randomUUID());

        recordAuditEventUseCase.execute(KafkaTopics.ORDER_CREATED, event, orderId);

        assertThat(auditEventRepository.findByAggregateId(orderId)).singleElement()
                .satisfies(saved -> assertThat(saved.getEventType()).isEqualTo(OrderCreatedEvent.TYPE));
    }

    @Test
    void recordsFraudApprovedEvent() {

        UUID orderId = UUID.randomUUID();
        FraudApprovedEvent event = Fixtures.fraudApproved(orderId, UUID.randomUUID());

        recordAuditEventUseCase.execute(KafkaTopics.FRAUD_APPROVED, event, orderId);

        assertThat(auditEventRepository.findByAggregateId(orderId)).singleElement()
                .satisfies(saved -> assertThat(saved.getEventType()).isEqualTo(FraudApprovedEvent.TYPE));
    }

    @Test
    void recordsFraudRejectedEvent() {

        UUID orderId = UUID.randomUUID();
        FraudRejectedEvent event = Fixtures.fraudRejected(orderId);

        recordAuditEventUseCase.execute(KafkaTopics.FRAUD_REJECTED, event, orderId);

        assertThat(auditEventRepository.findByAggregateId(orderId)).singleElement()
                .satisfies(saved -> assertThat(saved.getEventType()).isEqualTo(FraudRejectedEvent.TYPE));
    }

    @Test
    void recordsInventoryReservedEvent() {

        UUID orderId = UUID.randomUUID();
        InventoryReservedEvent event = Fixtures.inventoryReserved(orderId, UUID.randomUUID());

        recordAuditEventUseCase.execute(KafkaTopics.INVENTORY_RESERVED, event, orderId);

        assertThat(auditEventRepository.findByAggregateId(orderId)).singleElement()
                .satisfies(saved -> assertThat(saved.getEventType()).isEqualTo(InventoryReservedEvent.TYPE));
    }

    @Test
    void recordsInventoryReservationFailedEvent() {

        UUID orderId = UUID.randomUUID();
        InventoryReservationFailedEvent event = Fixtures.inventoryReservationFailed(orderId);

        recordAuditEventUseCase.execute(KafkaTopics.INVENTORY_RESERVATION_FAILED, event, orderId);

        assertThat(auditEventRepository.findByAggregateId(orderId)).singleElement()
                .satisfies(saved -> assertThat(saved.getEventType()).isEqualTo(InventoryReservationFailedEvent.TYPE));
    }

    @Test
    void recordsPaymentApprovedEvent() {

        UUID orderId = UUID.randomUUID();
        PaymentApprovedEvent event = Fixtures.paymentApproved(orderId, UUID.randomUUID());

        recordAuditEventUseCase.execute(KafkaTopics.PAYMENT_APPROVED, event, orderId);

        assertThat(auditEventRepository.findByAggregateId(orderId)).singleElement()
                .satisfies(saved -> assertThat(saved.getEventType()).isEqualTo(PaymentApprovedEvent.TYPE));
    }

    @Test
    void recordsPaymentFailedEvent() {

        UUID orderId = UUID.randomUUID();
        PaymentFailedEvent event = Fixtures.paymentFailed(orderId, UUID.randomUUID());

        recordAuditEventUseCase.execute(KafkaTopics.PAYMENT_FAILED, event, orderId);

        assertThat(auditEventRepository.findByAggregateId(orderId)).singleElement()
                .satisfies(saved -> assertThat(saved.getEventType()).isEqualTo(PaymentFailedEvent.TYPE));
    }

    @Test
    void inboxDeduplicatesRepeatedDelivery() {

        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, UUID.randomUUID());

        orderCreatedConsumer.consume(event);
        orderCreatedConsumer.consume(event);

        assertThat(auditEventRepository.findByAggregateId(orderId)).hasSize(1);
        assertThat(inboxEventRepository.findByEventId(event.eventId().toString())).isPresent();
    }
}
