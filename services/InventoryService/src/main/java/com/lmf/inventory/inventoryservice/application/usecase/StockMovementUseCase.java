package com.lmf.inventory.inventoryservice.application.usecase;

import com.lmf.inventory.inventoryservice.application.command.StockMovementCommand;
import com.lmf.inventory.inventoryservice.domain.model.Product;

public interface StockMovementUseCase {

    Product execute(StockMovementCommand stockMovementCommand);
}
