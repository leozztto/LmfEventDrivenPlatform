package com.lmf.notification.notificationservice.unit.infrastructure.kafka;

import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.usecase.NotifyInventoryReservationFailedUseCase;
import com.lmf.notification.notificationservice.infrastructure.kafka.consumer.InventoryReservationFailedConsumer;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.messaging.InboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InventoryReservationFailedConsumerTest {

    private InboxService inboxService;

    private NotifyInventoryReservationFailedUseCase useCase;

    private InventoryReservationFailedConsumer consumer;

    @BeforeEach
    void setUp() {
        inboxService = mock(InboxService.class);
        useCase = mock(NotifyInventoryReservationFailedUseCase.class);
        consumer = new InventoryReservationFailedConsumer(inboxService, useCase);
    }

    @Test
    void registersProcessesAndMarksOnHappyPath() {

        InventoryReservationFailedEvent event = Fixtures.inventoryReservationFailed(UUID.randomUUID());
        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        consumer.consume(event);

        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(useCase).execute(event);
        verify(inboxService).markProcessed(event.eventId().toString());
    }

    @Test
    void ignoresAlreadyProcessedEvent() {

        InventoryReservationFailedEvent event = Fixtures.inventoryReservationFailed(UUID.randomUUID());
        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(true);

        consumer.consume(event);

        verify(inboxService, never()).register(any(), any(), any());
        verifyNoInteractions(useCase);
    }

    @Test
    void propagatesFailureAndDoesNotMarkProcessed() {

        InventoryReservationFailedEvent event = Fixtures.inventoryReservationFailed(UUID.randomUUID());
        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);
        doThrow(new RuntimeException("boom")).when(useCase).execute(any());

        assertThatThrownBy(() -> consumer.consume(event)).isInstanceOf(RuntimeException.class).hasMessage("boom");

        verify(inboxService, never()).markProcessed(any());
    }
}
