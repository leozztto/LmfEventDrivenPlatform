package com.lmf.order.orderservice.domain.model.order;

public enum OrderStatus {

    PENDING_PAYMENT, PAYMENT_APPROVED, PAYMENT_REJECTED, INVENTORY_RESERVED, COMPLETED, CANCELLED;

    public static OrderStatus fromName(String name) {
        try {
            return OrderStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant with name: " + name + ". Available values: " + java.util.Arrays.toString(values()));
        }
    }

    public static OrderStatus[] getAllOutboxStatus() {
        return values();
    }
}
