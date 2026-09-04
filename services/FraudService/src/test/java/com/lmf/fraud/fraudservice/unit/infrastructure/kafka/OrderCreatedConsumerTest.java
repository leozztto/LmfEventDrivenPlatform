package com.lmf.fraud.fraudservice.unit.infrastructure.kafka;

import com.lmf.fraud.fraudservice.Fixtures;
import com.lmf.fraud.fraudservice.application.usecase.EvaluateFraudUseCase;
import com.lmf.fraud.fraudservice.infrastructure.kafka.consumer.OrderCreatedConsumer;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.InboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderCreatedConsumerTest {

    private InboxService inboxService;

    private EvaluateFraudUseCase evaluateFraudUseCase;

    private OrderCreatedConsumer orderCreatedConsumer;

    @BeforeEach
    void setup() {

        inboxService = mock(InboxService.class);
        evaluateFraudUseCase = mock(EvaluateFraudUseCase.class);

        orderCreatedConsumer = new OrderCreatedConsumer(inboxService, evaluateFraudUseCase);
    }

    @Test
    @DisplayName("Should process event successfully")
    void shouldProcessEventSuccessfully() {

        OrderCreatedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        orderCreatedConsumer.consume(event);

        verify(inboxService).isAlreadyProcessed(event.eventId().toString());
        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(evaluateFraudUseCase).execute(event);
        verify(inboxService).markProcessed(event.eventId().toString());
    }

    @Test
    @DisplayName("Should ignore an already-processed event")
    void shouldIgnoreDuplicateEvent() {

        OrderCreatedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(true);

        orderCreatedConsumer.consume(event);

        verify(inboxService).isAlreadyProcessed(event.eventId().toString());
        verifyNoInteractions(evaluateFraudUseCase);
        verify(inboxService, never()).register(any(), any(), any());
        verify(inboxService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Should propagate the exception when the use case fails")
    void shouldPropagateExceptionWhenUseCaseFails() {

        OrderCreatedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        RuntimeException runtimeException = new RuntimeException("fraud evaluation error");

        doThrow(runtimeException).when(evaluateFraudUseCase).execute(event);

        assertThatThrownBy(() -> orderCreatedConsumer.consume(event)).isSameAs(runtimeException);

        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(inboxService, never()).markProcessed(anyString());
    }

    private OrderCreatedEvent buildEvent() {

        return Fixtures.orderCreated(UUID.randomUUID(), UUID.randomUUID(), "ana@example.com", BigDecimal.valueOf(100));
    }
}
