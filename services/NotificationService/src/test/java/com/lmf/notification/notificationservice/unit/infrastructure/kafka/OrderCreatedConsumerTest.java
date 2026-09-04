package com.lmf.notification.notificationservice.unit.infrastructure.kafka;

import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.usecase.NotifyOrderCreatedUseCase;
import com.lmf.notification.notificationservice.infrastructure.kafka.consumer.OrderCreatedConsumer;
import com.lmf.platform.contracts.OrderCreatedEvent;
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

class OrderCreatedConsumerTest {

    private InboxService inboxService;

    private NotifyOrderCreatedUseCase useCase;

    private OrderCreatedConsumer consumer;

    @BeforeEach
    void setUp() {
        inboxService = mock(InboxService.class);
        useCase = mock(NotifyOrderCreatedUseCase.class);
        consumer = new OrderCreatedConsumer(inboxService, useCase);
    }

    @Test
    void registersProcessesAndMarksOnHappyPath() {

        OrderCreatedEvent event = Fixtures.orderCreated(UUID.randomUUID(), UUID.randomUUID());
        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        consumer.consume(event);

        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(useCase).execute(event);
        verify(inboxService).markProcessed(event.eventId().toString());
    }

    @Test
    void ignoresAlreadyProcessedEvent() {

        OrderCreatedEvent event = Fixtures.orderCreated(UUID.randomUUID(), UUID.randomUUID());
        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(true);

        consumer.consume(event);

        verify(inboxService, never()).register(any(), any(), any());
        verifyNoInteractions(useCase);
        verify(inboxService, never()).markProcessed(any());
    }

    @Test
    void propagatesFailureAndDoesNotMarkProcessed() {

        OrderCreatedEvent event = Fixtures.orderCreated(UUID.randomUUID(), UUID.randomUUID());
        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);
        doThrow(new RuntimeException("boom")).when(useCase).execute(any());

        assertThatThrownBy(() -> consumer.consume(event)).isInstanceOf(RuntimeException.class).hasMessage("boom");

        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(inboxService, never()).markProcessed(any());
    }
}
