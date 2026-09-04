package com.lmf.notification.notificationservice.unit.application.usecase;

import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.service.NotificationDispatchService;
import com.lmf.notification.notificationservice.application.service.NotificationMessageFactory;
import com.lmf.notification.notificationservice.application.service.NotifyPaymentApprovedService;
import com.lmf.notification.notificationservice.application.service.RecipientService;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.platform.contracts.PaymentApprovedEvent;
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

class NotifyPaymentApprovedServiceTest {

    private RecipientService recipientService;

    private NotificationDispatchService dispatchService;

    private NotifyPaymentApprovedService service;

    @BeforeEach
    void setUp() {
        recipientService = mock(RecipientService.class);
        dispatchService = mock(NotificationDispatchService.class);
        service = new NotifyPaymentApprovedService(recipientService, new NotificationMessageFactory(), dispatchService);
    }

    @Test
    void dispatchesWithResolvedRecipient() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        PaymentApprovedEvent event = Fixtures.paymentApproved(orderId, customerId);
        NotificationRecipient recipient = NotificationRecipient.of(orderId, customerId, "Ana", "ana@example.com", "1");
        when(recipientService.resolve(orderId)).thenReturn(Optional.of(recipient));

        service.execute(event);

        verify(dispatchService).dispatch(eq(orderId), eq(customerId), eq(NotificationType.PAYMENT_APPROVED), any(), eq(recipient));
    }

    @Test
    void dispatchesWithNullRecipientWhenUnknown() {

        UUID orderId = UUID.randomUUID();
        PaymentApprovedEvent event = Fixtures.paymentApproved(orderId, UUID.randomUUID());
        when(recipientService.resolve(orderId)).thenReturn(Optional.empty());

        service.execute(event);

        verify(dispatchService).dispatch(eq(orderId), any(), eq(NotificationType.PAYMENT_APPROVED), any(), isNull());
    }
}
