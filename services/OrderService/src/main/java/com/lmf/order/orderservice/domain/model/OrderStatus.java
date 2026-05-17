package com.lmf.order.orderservice.domain.model;

public enum OrderStatus {

    PENDING_PAYMENT,
    PAYMENT_APPROVED,
    PAYMENT_REJECTED,
    INVENTORY_RESERVED,
    COMPLETED,
    CANCELLED
}
