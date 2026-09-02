package com.lmf.inventory.inventoryservice.interfaces.rest.request;

import com.lmf.inventory.inventoryservice.application.command.ProductCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank String sku,

        @NotBlank String name,

        @NotBlank String description,

        @NotNull @Positive BigDecimal price,

        @NotNull @PositiveOrZero Integer initialStock) {

    public ProductCommand toProductCommand() {

        return new ProductCommand(sku, name, description, price, initialStock);
    }
}
