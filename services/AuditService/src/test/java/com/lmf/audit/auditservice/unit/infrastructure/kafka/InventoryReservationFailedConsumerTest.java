package com.lmf.audit.auditservice.unit.infrastructure.kafka;

import com.lmf.audit.auditservice.Fixtures;
import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.audit.auditservice.infrastructure.kafka.consumer.InventoryReservationFailedConsumer;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.messaging.InboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class InventoryReservationFailedConsumerTest {

    private InboxService inboxService;

    private RecordAuditEventUseCase recordAuditEventUseCase;

    private InventoryReservationFailedConsumer inventoryReservationFailedConsumer;

    @BeforeEach
    void setup() {

        inboxService = mock(InboxService.class);
        recordAuditEventUseCase = mock(RecordAuditEventUseCase.class);

        inventoryReservationFailedConsumer = new InventoryReservationFailedConsumer(inboxService, recordAuditEventUseCase);
    }

    @Test
    @DisplayName("Should process event successfully")
    void shouldProcessEventSuccessfully() {

        InventoryReservationFailedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        inventoryReservationFailedConsumer.consume(event);

        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(recordAuditEventUseCase).execute(KafkaTopics.INVENTORY_RESERVATION_FAILED, event, event.orderId());
        verify(inboxService).markProcessed(event.eventId().toString());
    }

    @Test
    @DisplayName("Should ignore an already-processed event")
    void shouldIgnoreDuplicateEvent() {

        InventoryReservationFailedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(true);

        inventoryReservationFailedConsumer.consume(event);

        verifyNoInteractions(recordAuditEventUseCase);
        verify(inboxService, never()).register(any(), any(), any());
        verify(inboxService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Should propagate the exception when the use case fails")
    void shouldPropagateExceptionWhenUseCaseFails() {

        InventoryReservationFailedEvent event = buildEvent();

        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        RuntimeException runtimeException = new RuntimeException("record audit event error");

        doThrow(runtimeException).when(recordAuditEventUseCase).execute(KafkaTopics.INVENTORY_RESERVATION_FAILED, event, event.orderId());

        assertThatThrownBy(() -> inventoryReservationFailedConsumer.consume(event)).isSameAs(runtimeException);

        verify(inboxService, never()).markProcessed(anyString());
    }

    private InventoryReservationFailedEvent buildEvent() {
        return Fixtures.inventoryReservationFailed(UUID.randomUUID());
    }
}
