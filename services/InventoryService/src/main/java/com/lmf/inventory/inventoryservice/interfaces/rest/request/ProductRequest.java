package com.lmf.inventory.inventoryservice.interfaces.rest.request;

import com.lmf.inventory.inventoryservice.application.command.ProductCommand;

import java.math.BigDecimal;

public record ProductRequest(

        String sku,

        String name,

        String description,

        BigDecimal price,

        Integer initialStock) {

    public ProductCommand toProductCommand() {

        return new ProductCommand(sku, name, description, price, initialStock);
    }
}