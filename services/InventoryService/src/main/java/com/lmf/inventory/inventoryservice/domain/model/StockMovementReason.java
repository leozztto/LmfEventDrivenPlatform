package com.lmf.inventory.inventoryservice.domain.model;

public enum StockMovementReason {

    PURCHASE, RETURN, DAMAGE, LOSS, INVENTORY_ADJUSTMENT, MANUAL;

    public static StockMovementReason fromName(String name) {
        try {
            return StockMovementReason.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant with name: " + name + ". Available values: " + java.util.Arrays.toString(values()));
        }
    }

    public static StockMovementReason[] getAllOutboxStatus() {
        return values();
    }
}
