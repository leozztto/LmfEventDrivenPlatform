package com.lmf.order.orderservice.infrastructure.web.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderResponse(UUID orderId, String status, BigDecimal totalAmount) {
}