package com.lmf.notification.notificationservice.application.service;

import com.lmf.notification.notificationservice.application.usecase.NotifyPaymentFailedUseCase;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.platform.contracts.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotifyPaymentFailedService implements NotifyPaymentFailedUseCase {

    private final RecipientService recipientService;

    private final NotificationMessageFactory messageFactory;

    private final NotificationDispatchService dispatchService;

    @Override
    @Transactional
    public void execute(PaymentFailedEvent event) {

        dispatchService.dispatch(event.orderId(), event.customerId(), NotificationType.PAYMENT_FAILED,
                messageFactory.paymentFailed(event), recipientService.resolve(event.orderId()).orElse(null));
    }
}
