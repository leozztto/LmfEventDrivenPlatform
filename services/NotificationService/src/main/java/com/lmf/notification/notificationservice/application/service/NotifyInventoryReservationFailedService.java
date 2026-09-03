package com.lmf.notification.notificationservice.application.service;

import com.lmf.notification.notificationservice.application.usecase.NotifyInventoryReservationFailedUseCase;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotifyInventoryReservationFailedService implements NotifyInventoryReservationFailedUseCase {

    private final RecipientService recipientService;

    private final NotificationMessageFactory messageFactory;

    private final NotificationDispatchService dispatchService;

    @Override
    @Transactional
    public void execute(InventoryReservationFailedEvent event) {

        NotificationRecipient recipient = recipientService.resolve(event.orderId()).orElse(null);

        UUID customerId = recipient != null ? recipient.getCustomerId() : null;

        dispatchService.dispatch(event.orderId(), customerId, NotificationType.INVENTORY_RESERVATION_FAILED,
                messageFactory.inventoryReservationFailed(event), recipient);
    }
}
