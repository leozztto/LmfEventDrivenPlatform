package com.lmf.inventory.inventoryservice.application.usecase;

import com.lmf.platform.contracts.FraudApprovedEvent;

public interface ReserveInventoryUseCase {

    void execute(FraudApprovedEvent fraudApprovedEvent);
}
