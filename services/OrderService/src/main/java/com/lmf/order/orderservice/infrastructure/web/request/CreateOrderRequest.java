package com.lmf.order.orderservice.infrastructure.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(

        @Valid @NotNull CustomerRequest customer,

        @Valid @NotNull ShippingAddressRequest shippingAddress,

        @Valid @NotNull PaymentRequest payment,

        @Valid @NotEmpty List<OrderItemRequest> items) {
}