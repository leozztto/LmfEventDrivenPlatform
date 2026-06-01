package com.lmf.inventory.inventoryservice.domain.model;

public enum ProductStatus {

    ACTIVE, INACTIVE;

    public static ProductStatus fromName(String name) {
        try {
            return ProductStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant with name: " + name + ". Available values: " + java.util.Arrays.toString(values()));
        }
    }

    public static ProductStatus[] getAllOutboxStatus() {
        return values();
    }
}
