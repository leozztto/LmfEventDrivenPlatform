package com.lmf.notification.notificationservice.unit.application.usecase;

import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.service.NotificationDispatchService;
import com.lmf.notification.notificationservice.application.service.NotificationMessageFactory;
import com.lmf.notification.notificationservice.application.service.NotifyOrderCreatedService;
import com.lmf.notification.notificationservice.application.service.RecipientService;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.platform.contracts.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotifyOrderCreatedServiceTest {

    private RecipientService recipientService;

    private NotificationDispatchService dispatchService;

    private NotifyOrderCreatedService service;

    @BeforeEach
    void setUp() {
        recipientService = mock(RecipientService.class);
        dispatchService = mock(NotificationDispatchService.class);
        service = new NotifyOrderCreatedService(recipientService, new NotificationMessageFactory(), dispatchService);
    }

    @Test
    void upsertsRecipientAndDispatchesOrderCreated() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, customerId);
        NotificationRecipient recipient = NotificationRecipient.of(orderId, customerId, "Ana", "ana@example.com", "1");
        when(recipientService.upsertFromOrder(event)).thenReturn(recipient);

        service.execute(event);

        verify(recipientService).upsertFromOrder(event);
        verify(dispatchService).dispatch(eq(orderId), eq(customerId), eq(NotificationType.ORDER_CREATED), any(), eq(recipient));
    }
}
