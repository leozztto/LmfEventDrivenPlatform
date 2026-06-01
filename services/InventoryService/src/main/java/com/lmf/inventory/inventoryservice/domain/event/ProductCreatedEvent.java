package com.lmf.inventory.inventoryservice.domain.event;

import com.lmf.inventory.inventoryservice.domain.model.Product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCreatedEvent(

        UUID productId,

        String sku,

        String name,

        BigDecimal price) {

    public static ProductCreatedEvent of(Product product) {

        return new ProductCreatedEvent(product.getId(), product.getSku(), product.getName(), product.getPrice());
    }
}
