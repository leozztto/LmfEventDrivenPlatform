package com.lmf.order.orderservice.domain.model;

import com.lmf.order.orderservice.domain.exception.EmptyOrderException;
import com.lmf.order.orderservice.domain.exception.InvalidOrderStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class Order {

    private final UUID id;
    private final UUID customerId;
    private final List<OrderItem> orderItems;

    private OrderStatus orderStatus;
    private BigDecimal totalAmount;

    private final OffsetDateTime createdAt;

    public Order(UUID customerId, List<OrderItem> orderItems) {

        validate(orderItems);

        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.orderItems = orderItems;

        this.orderStatus = OrderStatus.PENDING_PAYMENT;
        this.totalAmount = calculateTotal();

        this.createdAt = OffsetDateTime.now();
    }

    public Order(UUID id, UUID customerId, List<OrderItem> orderItems, OrderStatus orderStatus, OffsetDateTime createdAt) {

        validate(orderItems);

        this.id = id;
        this.customerId = customerId;
        this.orderItems = orderItems;

        this.orderStatus = orderStatus;
        this.createdAt = createdAt;

        this.totalAmount = calculateTotal();
    }

    private void validate(List<OrderItem> items) {

        if (items == null || items.isEmpty()) {
            throw new EmptyOrderException();
        }
    }

    private BigDecimal calculateTotal() {

        return orderItems.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void approvePayment() {

        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusException(orderStatus.name());
        }

        this.orderStatus = OrderStatus.PAYMENT_APPROVED;
    }

    public void rejectPayment() {

        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusException(orderStatus.name());
        }

        this.orderStatus = OrderStatus.PAYMENT_REJECTED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
