package com.lmf.audit.auditservice.unit.infrastructure.kafka;

import com.lmf.audit.auditservice.Fixtures;
import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.audit.auditservice.infrastructure.kafka.consumer.PaymentFailedConsumer;
import com.lmf.platform.contracts.PaymentFailedEvent;
import com.lmf.platform.messaging.InboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PaymentFailedConsumerTest {

    private InboxService inboxService;

    private RecordAuditEventUseCase recordAuditEventUseCase;

    private PaymentFailedConsumer paymentFailedConsumer;

    @BeforeEach
    void setup() {

        inboxService = mock(InboxService.class);
        recordAuditEventUseCase = mock(RecordAuditEventUseCase.class);

        paymentFailedConsumer = new PaymentFailedConsumer(inboxService, recordAuditEventUseCase);
    }

    @Test
    @DisplayName("Should process event successfully")
    void shouldProcessEventSuccessfully() {

        PaymentFailedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        paymentFailedConsumer.consume(event);

        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(recordAuditEventUseCase).execute(KafkaTopics.PAYMENT_FAILED, event, event.orderId());
        verify(inboxService).markProcessed(event.eventId().toString());
    }

    @Test
    @DisplayName("Should ignore an already-processed event")
    void shouldIgnoreDuplicateEvent() {

        PaymentFailedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(true);

        paymentFailedConsumer.consume(event);

        verifyNoInteractions(recordAuditEventUseCase);
        verify(inboxService, never()).register(any(), any(), any());
        verify(inboxService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Should propagate the exception when the use case fails")
    void shouldPropagateExceptionWhenUseCaseFails() {

        PaymentFailedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        RuntimeException runtimeException = new RuntimeException("record audit event error");

        doThrow(runtimeException).when(recordAuditEventUseCase).execute(KafkaTopics.PAYMENT_FAILED, event, event.orderId());

        assertThatThrownBy(() -> paymentFailedConsumer.consume(event)).isSameAs(runtimeException);

        verify(inboxService, never()).markProcessed(anyString());
    }

    private PaymentFailedEvent buildEvent() {
        return Fixtures.paymentFailed(UUID.randomUUID(), UUID.randomUUID());
    }
}
