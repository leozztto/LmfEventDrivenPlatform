package com.lmf.inventory.inventoryservice.domain.event.order;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {

    private final UUID productId;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public OrderItem(UUID productId, Integer quantity, BigDecimal unitPrice) {

        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = calculateSubtotal();
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
