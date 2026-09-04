package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.messaging.InboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FraudApprovedConsumerTest {

    private InboxService inboxEventService;

    private ReserveInventoryUseCase reserveInventoryUseCase;

    private FraudApprovedConsumer fraudApprovedConsumer;

    @BeforeEach
    void setup() {

        inboxEventService = mock(InboxService.class);
        reserveInventoryUseCase = mock(ReserveInventoryUseCase.class);

        fraudApprovedConsumer = new FraudApprovedConsumer(inboxEventService, reserveInventoryUseCase);
    }

    @Test
    @DisplayName("Should process event successfully")
    void shouldProcessEventSuccessfully() {

        FraudApprovedEvent fraudApprovedEvent = buildEvent();

        when(inboxEventService.isAlreadyProcessed(fraudApprovedEvent.eventId().toString())).thenReturn(false);

        fraudApprovedConsumer.consume(fraudApprovedEvent);

        verify(inboxEventService).isAlreadyProcessed(fraudApprovedEvent.eventId().toString());
        verify(inboxEventService).register(fraudApprovedEvent.eventId().toString(), fraudApprovedEvent.orderId(), fraudApprovedEvent.eventType());
        verify(reserveInventoryUseCase).execute(fraudApprovedEvent);
        verify(inboxEventService).markProcessed(fraudApprovedEvent.eventId().toString());
    }

    @Test
    @DisplayName("Should ignore an already-processed event")
    void shouldIgnoreDuplicateEvent() {

        FraudApprovedEvent fraudApprovedEvent = buildEvent();

        when(inboxEventService.isAlreadyProcessed(fraudApprovedEvent.eventId().toString())).thenReturn(true);

        fraudApprovedConsumer.consume(fraudApprovedEvent);

        verify(inboxEventService).isAlreadyProcessed(fraudApprovedEvent.eventId().toString());
        verifyNoInteractions(reserveInventoryUseCase);
        verify(inboxEventService, never()).register(any(), any(), any());
        verify(inboxEventService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Should propagate the exception when the use case fails")
    void shouldPropagateExceptionWhenUseCaseFails() {

        FraudApprovedEvent fraudApprovedEvent = buildEvent();

        when(inboxEventService.isAlreadyProcessed(fraudApprovedEvent.eventId().toString())).thenReturn(false);

        RuntimeException runtimeException = new RuntimeException("inventory reservation error");

        doThrow(runtimeException).when(reserveInventoryUseCase).execute(fraudApprovedEvent);

        assertThatThrownBy(() -> fraudApprovedConsumer.consume(fraudApprovedEvent)).isSameAs(runtimeException);

        verify(inboxEventService).register(fraudApprovedEvent.eventId().toString(), fraudApprovedEvent.orderId(), fraudApprovedEvent.eventType());
        verify(inboxEventService, never()).markProcessed(anyString());
    }

    private FraudApprovedEvent buildEvent() {

        return new FraudApprovedEvent(UUID.randomUUID(), FraudApprovedEvent.TYPE, "v1", OffsetDateTime.now(),
                UUID.randomUUID(), null, BigDecimal.valueOf(100), null, Collections.emptyList());
    }
}
