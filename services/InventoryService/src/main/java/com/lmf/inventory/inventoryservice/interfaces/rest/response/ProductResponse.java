package com.lmf.inventory.inventoryservice.interfaces.rest.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductResponse(

        UUID id,

        String sku,

        String name,

        String description,

        BigDecimal price,

        Integer availableQuantity,

        Integer reservedQuantity,

        String status,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt) {
}
