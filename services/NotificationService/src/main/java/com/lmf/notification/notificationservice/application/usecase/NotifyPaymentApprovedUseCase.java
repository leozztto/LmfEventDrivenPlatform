package com.lmf.notification.notificationservice.application.usecase;

import com.lmf.platform.contracts.PaymentApprovedEvent;

public interface NotifyPaymentApprovedUseCase {

    void execute(PaymentApprovedEvent event);
}
