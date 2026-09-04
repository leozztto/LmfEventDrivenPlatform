package com.lmf.notification.notificationservice.application.usecase;

import com.lmf.platform.contracts.PaymentFailedEvent;

public interface NotifyPaymentFailedUseCase {

    void execute(PaymentFailedEvent event);
}
