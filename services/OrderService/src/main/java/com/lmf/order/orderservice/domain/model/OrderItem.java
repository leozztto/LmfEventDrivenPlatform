package com.lmf.order.orderservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {

    private final UUID productId;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public OrderItem(
            UUID productId,
            Integer quantity,
            BigDecimal unitPrice
    ) {

        validate(quantity, unitPrice);

        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = calculateSubtotal();
    }

    private void validate(
            Integer quantity,
            BigDecimal unitPrice
    ) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
    }

    private BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
