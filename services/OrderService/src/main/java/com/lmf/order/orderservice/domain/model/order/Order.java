package com.lmf.order.orderservice.domain.model.order;

import com.lmf.order.orderservice.domain.exception.EmptyOrderException;
import com.lmf.order.orderservice.domain.exception.InvalidOrderStatusException;
import com.lmf.order.orderservice.domain.model.payment.PaymentInfo;
import com.lmf.order.orderservice.domain.model.customer.ShippingAddress;
import com.lmf.order.orderservice.domain.model.customer.CustomerInfo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class Order {

    private final UUID id;
    private final CustomerInfo customerInfo;
    private final ShippingAddress shippingAddress;
    private final PaymentInfo paymentInfo;
    private final List<OrderItem> orderItems;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;

    private final OffsetDateTime createdAt;

    public Order(CustomerInfo customerInfo, ShippingAddress shippingAddress, PaymentInfo paymentInfo, List<OrderItem> orderItems) {

        validate(orderItems);

        this.id = UUID.randomUUID();
        this.customerInfo = customerInfo;
        this.shippingAddress = shippingAddress;
        this.paymentInfo = paymentInfo;
        this.orderItems = orderItems;
        this.orderStatus = OrderStatus.PENDING_PAYMENT;
        this.totalAmount = calculateTotal();
        this.createdAt = OffsetDateTime.now();
    }

    public Order(UUID id, CustomerInfo customerInfo, ShippingAddress shippingAddress, PaymentInfo paymentInfo, List<OrderItem> orderItems, OrderStatus orderStatus, OffsetDateTime createdAt) {

        validate(orderItems);

        this.id = id;
        this.customerInfo = customerInfo;
        this.shippingAddress = shippingAddress;
        this.paymentInfo = paymentInfo;
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

    /**
     * Cancela o pedido — usado quando a reserva de estoque falha antes mesmo do pagamento.
     */
    public void cancel() {

        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusException(orderStatus.name());
        }

        this.orderStatus = OrderStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
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
