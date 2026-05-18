package com.lmf.order.orderservice.application.usecase.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(String idempotencyKey, UUID customerId, List<OrderItemCommand> items) {

    public record OrderItemCommand(UUID productId, Integer quantity, BigDecimal unitPrice) {
    }
}