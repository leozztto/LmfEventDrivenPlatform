package com.lmf.inventory.inventoryservice.application.usecase;

import com.lmf.inventory.inventoryservice.domain.event.OrderCreatedEvent;

public interface ReserveInventoryUseCase {

    void execute(OrderCreatedEvent orderCreatedEvent);
}
