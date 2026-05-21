package com.lmf.order.orderservice.infrastructure.web.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequest(

        @NotNull(message = "ProductId is required") UUID productId,

        @NotNull(message = "Quantity is required") Integer quantity,

        @NotNull(message = "Unit price is required") BigDecimal unitPrice) {
}
