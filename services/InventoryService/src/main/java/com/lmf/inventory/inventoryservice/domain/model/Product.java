package com.lmf.inventory.inventoryservice.domain.model;

import com.lmf.inventory.inventoryservice.domain.exception.InvalidProductException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class Product {

    private UUID id;

    private String sku;

    private String name;

    private String description;

    private BigDecimal price;

    private ProductStock productStock;

    private ProductStatus productStatus;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public static Product create(String sku, String name, String description, BigDecimal price, Integer initialStock) {

        validate(sku, name, price, initialStock);

        Product product = new Product();

        product.id = UUID.randomUUID();
        product.sku = sku;
        product.name = name;
        product.description = description;
        product.price = price;

        product.productStock = new ProductStock(initialStock, 0);

        product.productStatus = ProductStatus.ACTIVE;

        product.createdAt = OffsetDateTime.now();
        product.updatedAt = OffsetDateTime.now();

        return product;
    }

    public static Product restore(UUID id, String sku, String name, String description, BigDecimal price, Integer availableQuantity, Integer reservedQuantity, ProductStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {

        Product product = new Product();

        product.id = id;
        product.sku = sku;
        product.name = name;
        product.description = description;
        product.price = price;

        product.productStock = new ProductStock(availableQuantity, reservedQuantity);

        product.productStatus = status;
        product.createdAt = createdAt;
        product.updatedAt = updatedAt;

        return product;
    }

    private static void validate(String sku, String name, BigDecimal price, Integer initialStock) {

        if (sku == null || sku.isBlank()) {
            throw new InvalidProductException("SKU is required");
        }

        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Name is required");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductException("Price must be greater than zero");
        }

        if (initialStock == null || initialStock < 0) {
            throw new InvalidProductException("Initial stock cannot be negative");
        }
    }

    public void reserveStock(Integer quantity) {

        productStock.reserve(quantity);

        updatedAt = OffsetDateTime.now();
    }

    public void releaseStock(Integer quantity) {

        productStock.release(quantity);

        updatedAt = OffsetDateTime.now();
    }

    public void confirmReservedStock(Integer quantity) {

        productStock.confirm(quantity);

        updatedAt = OffsetDateTime.now();
    }

    public void addStock(Integer quantity) {

        productStock.increase(quantity);

        updatedAt = OffsetDateTime.now();
    }

    public void removeStock(Integer quantity) {

        productStock.decrease(quantity);

        updatedAt = OffsetDateTime.now();
    }
}
