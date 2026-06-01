package com.lmf.inventory.inventoryservice.domain.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String sku) {

        super("Product already exists with SKU: " + sku);
    }
}
