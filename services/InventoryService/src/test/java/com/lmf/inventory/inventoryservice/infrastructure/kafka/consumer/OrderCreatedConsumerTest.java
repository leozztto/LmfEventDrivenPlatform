package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.platform.contracts.OrderCreatedEvent;
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

class OrderCreatedConsumerTest {

    private InboxService inboxEventService;

    private ReserveInventoryUseCase reserveInventoryUseCase;

    private OrderCreatedConsumer orderCreatedConsumer;

    @BeforeEach
    void setup() {

        inboxEventService = mock(InboxService.class);
        reserveInventoryUseCase = mock(ReserveInventoryUseCase.class);

        orderCreatedConsumer = new OrderCreatedConsumer(inboxEventService, reserveInventoryUseCase);
    }

    @Test
    @DisplayName("Should process event successfully")
    void shouldProcessEventSuccessfully() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isAlreadyProcessed(orderCreatedEvent.eventId().toString())).thenReturn(false);

        orderCreatedConsumer.consume(orderCreatedEvent);

        verify(inboxEventService).isAlreadyProcessed(orderCreatedEvent.eventId().toString());
        verify(inboxEventService).register(orderCreatedEvent.eventId().toString(), orderCreatedEvent.orderId(), orderCreatedEvent.eventType());
        verify(reserveInventoryUseCase).execute(orderCreatedEvent);
        verify(inboxEventService).markProcessed(orderCreatedEvent.eventId().toString());
    }

    @Test
    @DisplayName("Should ignore an already-processed event")
    void shouldIgnoreDuplicateEvent() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isAlreadyProcessed(orderCreatedEvent.eventId().toString())).thenReturn(true);

        orderCreatedConsumer.consume(orderCreatedEvent);

        verify(inboxEventService).isAlreadyProcessed(orderCreatedEvent.eventId().toString());
        verifyNoInteractions(reserveInventoryUseCase);
        verify(inboxEventService, never()).register(any(), any(), any());
        verify(inboxEventService, never()).markProcessed(any());
    }

    @Test
    @DisplayName("Should propagate the exception when the use case fails")
    void shouldPropagateExceptionWhenUseCaseFails() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isAlreadyProcessed(orderCreatedEvent.eventId().toString())).thenReturn(false);

        RuntimeException runtimeException = new RuntimeException("inventory reservation error");

        doThrow(runtimeException).when(reserveInventoryUseCase).execute(orderCreatedEvent);

        assertThatThrownBy(() -> orderCreatedConsumer.consume(orderCreatedEvent)).isSameAs(runtimeException);

        verify(inboxEventService).register(orderCreatedEvent.eventId().toString(), orderCreatedEvent.orderId(), orderCreatedEvent.eventType());
        verify(inboxEventService, never()).markProcessed(anyString());
    }

    private OrderCreatedEvent buildEvent() {

        return new OrderCreatedEvent(UUID.randomUUID(), OrderCreatedEvent.TYPE, "v1", OffsetDateTime.now(), UUID.randomUUID(), "PENDING_PAYMENT", BigDecimal.valueOf(100), null, null, null, Collections.emptyList());
    }
}
