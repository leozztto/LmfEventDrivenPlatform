package com.lmf.audit.auditservice.unit.infrastructure.kafka;

import com.lmf.audit.auditservice.Fixtures;
import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.audit.auditservice.infrastructure.kafka.consumer.OrderCreatedConsumer;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.InboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderCreatedConsumerTest {

    private InboxService inboxService;

    private RecordAuditEventUseCase recordAuditEventUseCase;

    private OrderCreatedConsumer orderCreatedConsumer;

    @BeforeEach
    void setup() {

        inboxService = mock(InboxService.class);
        recordAuditEventUseCase = mock(RecordAuditEventUseCase.class);

        orderCreatedConsumer = new OrderCreatedConsumer(inboxService, recordAuditEventUseCase);
    }

    @Test
    @DisplayName("Should process event successfully")
    void shouldProcessEventSuccessfully() {

        OrderCreatedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        orderCreatedConsumer.consume(event);

        verify(inboxService).isAlreadyProcessed(event.eventId().toString());
        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(recordAuditEventUseCase).execute(KafkaTopics.ORDER_CREATED, event, event.orderId());
        verify(inboxService).markProcessed(event.eventId().toString());
    }

    @Test
    @DisplayName("Should ignore an already-processed event")
    void shouldIgnoreDuplicateEvent() {

        OrderCreatedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(true);

        orderCreatedConsumer.consume(event);

        verify(inboxService).isAlreadyProcessed(event.eventId().toString());
        verifyNoInteractions(recordAuditEventUseCase);
        verify(inboxService, never()).register(any(), any(), any());
        verify(inboxService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Should propagate the exception when the use case fails")
    void shouldPropagateExceptionWhenUseCaseFails() {

        OrderCreatedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        RuntimeException runtimeException = new RuntimeException("record audit event error");

        doThrow(runtimeException).when(recordAuditEventUseCase).execute(KafkaTopics.ORDER_CREATED, event, event.orderId());

        assertThatThrownBy(() -> orderCreatedConsumer.consume(event)).isSameAs(runtimeException);

        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(inboxService, never()).markProcessed(anyString());
    }

    private OrderCreatedEvent buildEvent() {
        return Fixtures.orderCreated(UUID.randomUUID(), UUID.randomUUID());
    }
}
