package com.lmf.order.orderservice.application.usecase.result;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateOrderResult(

        UUID orderId,

        String status,

        BigDecimal totalAmount,

        OffsetDateTime createdAt) {
}