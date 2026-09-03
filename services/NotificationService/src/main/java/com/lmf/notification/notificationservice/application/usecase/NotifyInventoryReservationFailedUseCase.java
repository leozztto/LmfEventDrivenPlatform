package com.lmf.notification.notificationservice.application.usecase;

import com.lmf.platform.contracts.InventoryReservationFailedEvent;

public interface NotifyInventoryReservationFailedUseCase {

    void execute(InventoryReservationFailedEvent event);
}
