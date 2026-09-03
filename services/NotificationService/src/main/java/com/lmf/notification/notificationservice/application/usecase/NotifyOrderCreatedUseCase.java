package com.lmf.notification.notificationservice.application.usecase;

import com.lmf.platform.contracts.OrderCreatedEvent;

public interface NotifyOrderCreatedUseCase {

    void execute(OrderCreatedEvent event);
}
