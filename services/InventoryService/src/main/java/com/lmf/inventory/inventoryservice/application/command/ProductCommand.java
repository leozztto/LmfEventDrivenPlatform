package com.lmf.inventory.inventoryservice.application.command;

import java.math.BigDecimal;

public record ProductCommand(

        String sku,

        String name,

        String description,

        BigDecimal price,

        Integer initialStock) {
}
