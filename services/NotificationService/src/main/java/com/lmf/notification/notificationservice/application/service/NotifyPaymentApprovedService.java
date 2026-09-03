package com.lmf.notification.notificationservice.application.service;

import com.lmf.notification.notificationservice.application.usecase.NotifyPaymentApprovedUseCase;
import com.lmf.notification.notificationservice.domain.model.NotificationType;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotifyPaymentApprovedService implements NotifyPaymentApprovedUseCase {

    private final RecipientService recipientService;

    private final NotificationMessageFactory messageFactory;

    private final NotificationDispatchService dispatchService;

    @Override
    @Transactional
    public void execute(PaymentApprovedEvent event) {

        dispatchService.dispatch(event.orderId(), event.customerId(), NotificationType.PAYMENT_APPROVED,
                messageFactory.paymentApproved(event), recipientService.resolve(event.orderId()).orElse(null));
    }
}
