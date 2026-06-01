package com.lmf.inventory.inventoryservice.domain.model;

public enum StockMovementType {

    IN, OUT;

    public static StockMovementType fromName(String name) {
        try {
            return StockMovementType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant with name: " + name + ". Available values: " + java.util.Arrays.toString(values()));
        }
    }

    public static StockMovementType[] getAllOutboxStatus() {
        return values();
    }
}
