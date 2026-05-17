package com.lmf.order.orderservice.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class Order {

    private final UUID id;
    private final UUID customerId;
    private final List<OrderItem> items;

    private OrderStatus status;
    private BigDecimal totalAmount;

    private final OffsetDateTime createdAt;

    public Order(
            UUID customerId,
            List<OrderItem> items
    ) {

        validate(items);

        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.items = items;

        this.status = OrderStatus.PENDING_PAYMENT;
        this.totalAmount = calculateTotal();

        this.createdAt = OffsetDateTime.now();
    }

    private void validate(List<OrderItem> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }

    private BigDecimal calculateTotal() {

        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void approvePayment() {

        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Invalid order status");
        }

        this.status = OrderStatus.PAYMENT_APPROVED;
    }

    public void rejectPayment() {

        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Invalid order status");
        }

        this.status = OrderStatus.PAYMENT_REJECTED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
