package com.lmf.fraud.fraudservice.integration;

import com.lmf.fraud.fraudservice.Fixtures;
import com.lmf.fraud.fraudservice.application.usecase.EvaluateFraudUseCase;
import com.lmf.fraud.fraudservice.infrastructure.kafka.consumer.OrderCreatedConsumer;
import com.lmf.fraud.fraudservice.infrastructure.persistence.repository.SpringDataFraudCheckRepository;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.InboxEventRepository;
import com.lmf.platform.messaging.OutboxEvent;
import com.lmf.platform.messaging.OutboxEventRepository;
import com.lmf.platform.messaging.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fluxo completo do FraudService a partir do use case (sem depender do broker real): grava o
 * histórico de decisão e o evento de saída correto no outbox. O seed do {@code V3} fica no banco em
 * todos os testes — os cenários abaixo usam identificadores próprios, nunca assumem a tabela vazia.
 */
class FraudFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EvaluateFraudUseCase evaluateFraudUseCase;

    @Autowired
    private OrderCreatedConsumer orderCreatedConsumer;

    @Autowired
    private SpringDataFraudCheckRepository springDataFraudCheckRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private InboxEventRepository inboxEventRepository;

    @BeforeEach
    void clean() {
        springDataFraudCheckRepository.deleteAll();
        outboxEventRepository.deleteAll();
        inboxEventRepository.deleteAll();
    }

    @Test
    void approvesOrderWithinLimitAndWritesFraudApprovedToOutbox() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderCreatedEvent event = Fixtures.orderCreated(orderId, customerId, "ana@example.com", new BigDecimal("250.00"));

        evaluateFraudUseCase.execute(event);

        assertThat(springDataFraudCheckRepository.findAll())
                .filteredOn(check -> check.getOrderId().equals(orderId))
                .singleElement()
                .satisfies(check -> assertThat(check.getDecision()).isEqualTo("APPROVED"));

        List<OutboxEvent> outbox = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        assertThat(outbox).filteredOn(o -> o.getAggregateId().equals(orderId))
                .singleElement()
                .satisfies(o -> assertThat(o.getEventType()).isEqualTo("FRAUD_APPROVED"));
    }

    @Test
    void rejectsOrderAboveLimitAndWritesFraudRejectedToOutbox() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        // Acima do fraud.rules.max-order-amount default (5000.00).
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, customerId, "ana@example.com", new BigDecimal("6000.00"));

        evaluateFraudUseCase.execute(event);

        assertThat(springDataFraudCheckRepository.findAll())
                .filteredOn(check -> check.getOrderId().equals(orderId))
                .singleElement()
                .satisfies(check -> assertThat(check.getDecision()).isEqualTo("REJECTED"));

        List<OutboxEvent> outbox = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        assertThat(outbox).filteredOn(o -> o.getAggregateId().equals(orderId))
                .singleElement()
                .satisfies(o -> assertThat(o.getEventType()).isEqualTo("FRAUD_REJECTED"));
    }

    @Test
    void rejectsOrderFromBlocklistedCustomerSeededByFlyway() {

        UUID orderId = UUID.randomUUID();

        // "blocked.customer@example.com" vem do seed V3__seed_fraud_blocklist.sql.
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, UUID.randomUUID(), "blocked.customer@example.com", new BigDecimal("50.00"));

        evaluateFraudUseCase.execute(event);

        assertThat(springDataFraudCheckRepository.findAll())
                .filteredOn(check -> check.getOrderId().equals(orderId))
                .singleElement()
                .satisfies(check -> {
                    assertThat(check.getDecision()).isEqualTo("REJECTED");
                    assertThat(check.getReason()).isEqualTo("Customer is blocklisted");
                });
    }

    @Test
    void inboxDeduplicatesRepeatedDelivery() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderCreatedEvent event = Fixtures.orderCreated(orderId, customerId, "ana@example.com", new BigDecimal("250.00"));

        orderCreatedConsumer.consume(event);
        orderCreatedConsumer.consume(event);

        assertThat(springDataFraudCheckRepository.findAll()).filteredOn(check -> check.getOrderId().equals(orderId)).hasSize(1);

        assertThat(inboxEventRepository.findByEventId(event.eventId().toString())).isPresent();
    }
}
