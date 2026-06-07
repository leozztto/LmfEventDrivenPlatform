package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.service.InboxEventService;
import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.inventory.inventoryservice.domain.event.OrderCreatedEvent;
import com.lmf.inventory.inventoryservice.domain.event.order.CustomerInfo;
import com.lmf.inventory.inventoryservice.domain.event.order.PaymentInfo;
import com.lmf.inventory.inventoryservice.domain.event.order.ShippingAddress;
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

    private InboxEventService inboxEventService;

    private ReserveInventoryUseCase reserveInventoryUseCase;

    private OrderCreatedConsumer orderCreatedConsumer;

    @BeforeEach
    void setup() {

        inboxEventService = mock(InboxEventService.class);
        reserveInventoryUseCase = mock(ReserveInventoryUseCase.class);

        orderCreatedConsumer = new OrderCreatedConsumer(inboxEventService, reserveInventoryUseCase);
    }

    @Test
    @DisplayName("Should process event successfully")
    void shouldProcessEventSuccessfully() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())).thenReturn(false);

        orderCreatedConsumer.consume(orderCreatedEvent);

        verify(inboxEventService).isDuplicate(orderCreatedEvent.eventId().toString());

        verify(inboxEventService).register(orderCreatedEvent.eventId().toString(), orderCreatedEvent.orderId(), orderCreatedEvent.eventType());

        verify(reserveInventoryUseCase).execute(orderCreatedEvent);

        verify(inboxEventService).markProcessed(orderCreatedEvent.eventId().toString());

        verify(inboxEventService, never()).markFailed(anyString(), anyString());
    }

    @Test
    @DisplayName("Should ignore duplicate event")
    void shouldIgnoreDuplicateEvent() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())).thenReturn(true);

        orderCreatedConsumer.consume(orderCreatedEvent);

        verify(inboxEventService).isDuplicate(orderCreatedEvent.eventId().toString());

        verifyNoInteractions(reserveInventoryUseCase);

        verify(inboxEventService, never()).register(any(), any(), any());

        verify(inboxEventService, never()).markProcessed(any());

        verify(inboxEventService, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("Should mark event as failed when use case throws exception")
    void shouldMarkEventAsFailed() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())).thenReturn(false);

        RuntimeException runtimeException = new RuntimeException("inventory reservation error");

        doThrow(runtimeException).when(reserveInventoryUseCase).execute(orderCreatedEvent);

        assertThatThrownBy(() -> orderCreatedConsumer.consume(orderCreatedEvent)).isSameAs(runtimeException);

        verify(inboxEventService).register(orderCreatedEvent.eventId().toString(), orderCreatedEvent.orderId(), orderCreatedEvent.eventType());

        verify(inboxEventService).markFailed(orderCreatedEvent.eventId().toString(), "inventory reservation error");

        verify(inboxEventService, never()).markProcessed(anyString());
    }

    @Test
    @DisplayName("Should use exception class name when exception message is null")
    void shouldUseExceptionClassNameWhenMessageIsNull() {

        OrderCreatedEvent orderCreatedEvent = buildEvent();

        when(inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())).thenReturn(false);

        RuntimeException runtimeException = new RuntimeException((String) null);

        doThrow(runtimeException).when(reserveInventoryUseCase).execute(orderCreatedEvent);

        assertThatThrownBy(() -> orderCreatedConsumer.consume(orderCreatedEvent)).isSameAs(runtimeException);

        verify(inboxEventService).markFailed(orderCreatedEvent.eventId().toString(), "RuntimeException");
    }

    private OrderCreatedEvent buildEvent() {

        return new OrderCreatedEvent(UUID.randomUUID(), "ORDER_CREATED", "1.0", OffsetDateTime.now(), UUID.randomUUID(), "CREATED", BigDecimal.valueOf(100), mock(CustomerInfo.class), mock(ShippingAddress.class), mock(PaymentInfo.class), Collections.emptyList());
    }
}