package com.lmf.notification.notificationservice.unit.application.usecase;

import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.service.NotificationDispatchService;
import com.lmf.notification.notificationservice.application.service.NotificationMessageFactory;
import com.lmf.notification.notificationservice.application.service.NotifyInventoryReservationFailedService;
import com.lmf.notification.notificationservice.application.service.RecipientService;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotifyInventoryReservationFailedServiceTest {

    private RecipientService recipientService;

    private NotificationDispatchService dispatchService;

    private NotifyInventoryReservationFailedService service;

    @BeforeEach
    void setUp() {
        recipientService = mock(RecipientService.class);
        dispatchService = mock(NotificationDispatchService.class);
        service = new NotifyInventoryReservationFailedService(recipientService, new NotificationMessageFactory(), dispatchService);
    }

    @Test
    void takesCustomerIdFromResolvedRecipient() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        InventoryReservationFailedEvent event = Fixtures.inventoryReservationFailed(orderId);
        NotificationRecipient recipient = NotificationRecipient.of(orderId, customerId, "Ana", "ana@example.com", "1");
        when(recipientService.resolve(orderId)).thenReturn(Optional.of(recipient));

        service.execute(event);

        verify(dispatchService).dispatch(eq(orderId), eq(customerId), eq(NotificationType.INVENTORY_RESERVATION_FAILED), any(), eq(recipient));
    }
}
