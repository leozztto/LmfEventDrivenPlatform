package com.lmf.inventory.inventoryservice.interfaces.rest;

import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.interfaces.rest.response.ProductResponse;

public final class ProductResponseMapper {

    private ProductResponseMapper() {
    }

    public static ProductResponse from(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getProductStock().getAvailableQuantity(),
                product.getProductStock().getReservedQuantity(),
                product.getProductStatus().name(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
