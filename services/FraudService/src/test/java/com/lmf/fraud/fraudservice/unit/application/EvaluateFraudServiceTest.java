package com.lmf.fraud.fraudservice.unit.application;

import com.lmf.fraud.fraudservice.Fixtures;
import com.lmf.fraud.fraudservice.application.service.EvaluateFraudService;
import com.lmf.fraud.fraudservice.domain.model.FraudCheck;
import com.lmf.fraud.fraudservice.domain.model.FraudDecision;
import com.lmf.fraud.fraudservice.domain.repository.FraudCheckRepository;
import com.lmf.fraud.fraudservice.domain.service.FraudRulesEvaluator;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.contracts.FraudRejectedEvent;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.OutboxWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateFraudServiceTest {

    @Mock
    private FraudRulesEvaluator fraudRulesEvaluator;

    @Mock
    private FraudCheckRepository fraudCheckRepository;

    @Mock
    private OutboxWriter outboxWriter;

    @InjectMocks
    private EvaluateFraudService evaluateFraudService;

    @Test
    @DisplayName("Should write FraudApprovedEvent to the outbox when the decision approves")
    void shouldWriteFraudApprovedEventWhenApproved() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderCreatedEvent event = Fixtures.orderCreated(orderId, customerId, "ana@example.com", new BigDecimal("250.00"));

        when(fraudRulesEvaluator.evaluate(customerId, "ana@example.com", event.totalAmount())).thenReturn(FraudDecision.approve());
        when(fraudCheckRepository.save(any(FraudCheck.class))).thenAnswer(invocation -> invocation.getArgument(0));

        evaluateFraudService.execute(event);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxWriter).write(eq(orderId), eq("ORDER"), eq(FraudApprovedEvent.TYPE), payloadCaptor.capture());

        assertThat(payloadCaptor.getValue()).isInstanceOf(FraudApprovedEvent.class);
        assertThat(((FraudApprovedEvent) payloadCaptor.getValue()).orderId()).isEqualTo(orderId);

        ArgumentCaptor<FraudCheck> fraudCheckCaptor = ArgumentCaptor.forClass(FraudCheck.class);
        verify(fraudCheckRepository).save(fraudCheckCaptor.capture());
        assertThat(fraudCheckCaptor.getValue().isApproved()).isTrue();
    }

    @Test
    @DisplayName("Should write FraudRejectedEvent to the outbox when the decision rejects")
    void shouldWriteFraudRejectedEventWhenRejected() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderCreatedEvent event = Fixtures.orderCreated(orderId, customerId, "blocked@example.com", new BigDecimal("6000.00"));

        when(fraudRulesEvaluator.evaluate(customerId, "blocked@example.com", event.totalAmount()))
                .thenReturn(FraudDecision.reject("Order amount exceeds limit"));
        when(fraudCheckRepository.save(any(FraudCheck.class))).thenAnswer(invocation -> invocation.getArgument(0));

        evaluateFraudService.execute(event);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxWriter).write(eq(orderId), eq("ORDER"), eq(FraudRejectedEvent.TYPE), payloadCaptor.capture());

        assertThat(payloadCaptor.getValue()).isInstanceOf(FraudRejectedEvent.class);
        assertThat(((FraudRejectedEvent) payloadCaptor.getValue()).reason()).isEqualTo("Order amount exceeds limit");

        ArgumentCaptor<FraudCheck> fraudCheckCaptor = ArgumentCaptor.forClass(FraudCheck.class);
        verify(fraudCheckRepository).save(fraudCheckCaptor.capture());
        assertThat(fraudCheckCaptor.getValue().isApproved()).isFalse();
    }
}
