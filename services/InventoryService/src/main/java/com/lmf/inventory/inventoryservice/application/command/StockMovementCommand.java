package com.lmf.inventory.inventoryservice.application.command;

import com.lmf.inventory.inventoryservice.domain.model.StockMovementReason;
import com.lmf.inventory.inventoryservice.domain.model.StockMovementType;

import java.util.UUID;

public record StockMovementCommand(

        UUID productId,

        StockMovementType stockMovementType,

        Integer quantity,

        StockMovementReason stockMovementReason) {
}
