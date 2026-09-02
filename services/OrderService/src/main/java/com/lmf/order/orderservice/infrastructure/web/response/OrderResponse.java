package com.lmf.order.orderservice.infrastructure.web.response;

import com.lmf.order.orderservice.domain.model.order.Order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(

        UUID orderId,

        String status,

        BigDecimal totalAmount,

        OffsetDateTime createdAt,

        UUID customerId,

        List<Item> items) {

    public record Item(UUID productId, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }

    public static OrderResponse from(Order order) {

        List<Item> items = order.getOrderItems().stream()
                .map(item -> new Item(item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getCustomerInfo().getCustomerId(),
                items);
    }
}
