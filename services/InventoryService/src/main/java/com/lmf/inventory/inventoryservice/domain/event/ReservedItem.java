package com.lmf.inventory.inventoryservice.domain.event;

import java.util.UUID;

public record ReservedItem(

        UUID productId,

        Integer quantity) {
}
