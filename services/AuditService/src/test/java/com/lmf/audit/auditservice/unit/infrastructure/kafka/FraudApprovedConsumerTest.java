package com.lmf.audit.auditservice.unit.infrastructure.kafka;

import com.lmf.audit.auditservice.Fixtures;
import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.audit.auditservice.infrastructure.kafka.consumer.FraudApprovedConsumer;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.messaging.InboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FraudApprovedConsumerTest {

    private InboxService inboxService;

    private RecordAuditEventUseCase recordAuditEventUseCase;

    private FraudApprovedConsumer fraudApprovedConsumer;

    @BeforeEach
    void setup() {

        inboxService = mock(InboxService.class);
        recordAuditEventUseCase = mock(RecordAuditEventUseCase.class);

        fraudApprovedConsumer = new FraudApprovedConsumer(inboxService, recordAuditEventUseCase);
    }

    @Test
    @DisplayName("Should process event successfully")
    void shouldProcessEventSuccessfully() {

        FraudApprovedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        fraudApprovedConsumer.consume(event);

        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(recordAuditEventUseCase).execute(KafkaTopics.FRAUD_APPROVED, event, event.orderId());
        verify(inboxService).markProcessed(event.eventId().toString());
    }

    @Test
    @DisplayName("Should ignore an already-processed event")
    void shouldIgnoreDuplicateEvent() {

        FraudApprovedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(true);

        fraudApprovedConsumer.consume(event);

        verifyNoInteractions(recordAuditEventUseCase);
        verify(inboxService, never()).register(any(), any(), any());
        verify(inboxService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Should propagate the exception when the use case fails")
    void shouldPropagateExceptionWhenUseCaseFails() {

        FraudApprovedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        RuntimeException runtimeException = new RuntimeException("record audit event error");

        doThrow(runtimeException).when(recordAuditEventUseCase).execute(KafkaTopics.FRAUD_APPROVED, event, event.orderId());

        assertThatThrownBy(() -> fraudApprovedConsumer.consume(event)).isSameAs(runtimeException);

        verify(inboxService, never()).markProcessed(anyString());
    }

    private FraudApprovedEvent buildEvent() {
        return Fixtures.fraudApproved(UUID.randomUUID(), UUID.randomUUID());
    }
}
