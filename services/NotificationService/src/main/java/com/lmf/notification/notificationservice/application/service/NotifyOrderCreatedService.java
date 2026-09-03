package com.lmf.notification.notificationservice.application.service;

import com.lmf.notification.notificationservice.application.usecase.NotifyOrderCreatedUseCase;
import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.platform.contracts.CustomerInfo;
import com.lmf.platform.contracts.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotifyOrderCreatedService implements NotifyOrderCreatedUseCase {

    private final RecipientService recipientService;

    private final NotificationMessageFactory messageFactory;

    private final NotificationDispatchService dispatchService;

    @Override
    @Transactional
    public void execute(OrderCreatedEvent event) {

        NotificationRecipient recipient = recipientService.upsertFromOrder(event);

        CustomerInfo customer = event.customer();
        UUID customerId = customer != null ? customer.customerId() : null;

        dispatchService.dispatch(event.orderId(), customerId, NotificationType.ORDER_CREATED,
                messageFactory.orderCreated(event), recipient);
    }
}
