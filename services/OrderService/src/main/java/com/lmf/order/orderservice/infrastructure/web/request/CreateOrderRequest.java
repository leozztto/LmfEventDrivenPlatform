package com.lmf.order.orderservice.infrastructure.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    @NotNull
    private UUID customerId;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;

    @Getter
    @NoArgsConstructor
    public static class OrderItemRequest {

        @NotNull
        private UUID productId;

        @NotNull
        @Positive
        private Integer quantity;

        @NotNull
        @Positive
        private BigDecimal unitPrice;
    }
}
