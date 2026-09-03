package com.lmf.notification.notificationservice.unit.infrastructure.kafka;

import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.usecase.NotifyPaymentApprovedUseCase;
import com.lmf.notification.notificationservice.infrastructure.kafka.consumer.PaymentApprovedConsumer;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.messaging.InboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentApprovedConsumerTest {

    private InboxService inboxService;

    private NotifyPaymentApprovedUseCase useCase;

    private PaymentApprovedConsumer consumer;

    @BeforeEach
    void setUp() {
        inboxService = mock(InboxService.class);
        useCase = mock(NotifyPaymentApprovedUseCase.class);
        consumer = new PaymentApprovedConsumer(inboxService, useCase);
    }

    @Test
    void registersProcessesAndMarksOnHappyPath() {

        PaymentApprovedEvent event = Fixtures.paymentApproved(UUID.randomUUID(), UUID.randomUUID());
        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(false);

        consumer.consume(event);

        verify(inboxService).register(event.eventId().toString(), event.orderId(), event.eventType());
        verify(useCase).execute(event);
        verify(inboxService).markProcessed(event.eventId().toString());
    }

    @Test
    void ignoresAlreadyProcessedEvent() {

        PaymentApprovedEvent event = Fixtures.paymentApproved(UUID.randomUUID(), UUID.randomUUID());
        when(inboxService.isAlreadyProcessed(event.eventId().toString())).thenReturn(true);

        consumer.consume(event);

        verify(inboxService, never()).register(any(), any(), any());
        verifyNoInteractions(useCase);
    }
}
