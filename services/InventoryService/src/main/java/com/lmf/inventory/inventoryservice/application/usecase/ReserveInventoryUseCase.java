package com.lmf.inventory.inventoryservice.application.usecase;

import com.lmf.platform.contracts.OrderCreatedEvent;

public interface ReserveInventoryUseCase {

    void execute(OrderCreatedEvent orderCreatedEvent);
}
