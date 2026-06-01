package com.lmf.inventory.inventoryservice.interfaces.rest.request;

import com.lmf.inventory.inventoryservice.domain.model.StockMovementReason;
import com.lmf.inventory.inventoryservice.domain.model.StockMovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record StockMovementRequest(

        @NotNull UUID productId,

        @NotNull StockMovementType stockMovementType,

        @NotNull @Positive Integer quantity,

        @NotNull StockMovementReason stockMovementReason) {
}
