package com.lmf.order.orderservice.domain.model.order;

public enum OrderStatus {

    PENDING_PAYMENT, PAYMENT_APPROVED, PAYMENT_REJECTED, FRAUD_REJECTED, INVENTORY_RESERVED, COMPLETED, CANCELLED
}
