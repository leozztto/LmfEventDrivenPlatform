package com.lmf.notification.notificationservice.unit.application.usecase;

import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.service.NotificationDispatchService;
import com.lmf.notification.notificationservice.application.service.NotificationMessageFactory;
import com.lmf.notification.notificationservice.application.service.NotifyPaymentFailedService;
import com.lmf.notification.notificationservice.application.service.RecipientService;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.platform.contracts.PaymentFailedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotifyPaymentFailedServiceTest {

    private RecipientService recipientService;

    private NotificationDispatchService dispatchService;

    private NotifyPaymentFailedService service;

    @BeforeEach
    void setUp() {
        recipientService = mock(RecipientService.class);
        dispatchService = mock(NotificationDispatchService.class);
        service = new NotifyPaymentFailedService(recipientService, new NotificationMessageFactory(), dispatchService);
    }

    @Test
    void dispatchesWithResolvedRecipient() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        PaymentFailedEvent event = Fixtures.paymentFailed(orderId, customerId);
        NotificationRecipient recipient = NotificationRecipient.of(orderId, customerId, "Ana", "ana@example.com", "1");
        when(recipientService.resolve(orderId)).thenReturn(Optional.of(recipient));

        service.execute(event);

        verify(dispatchService).dispatch(eq(orderId), eq(customerId), eq(NotificationType.PAYMENT_FAILED), any(), eq(recipient));
    }

    @Test
    void dispatchesWithNullRecipientWhenUnknown() {

        UUID orderId = UUID.randomUUID();
        PaymentFailedEvent event = Fixtures.paymentFailed(orderId, UUID.randomUUID());
        when(recipientService.resolve(orderId)).thenReturn(Optional.empty());

        service.execute(event);

        verify(dispatchService).dispatch(eq(orderId), any(), eq(NotificationType.PAYMENT_FAILED), any(), isNull());
    }
}
