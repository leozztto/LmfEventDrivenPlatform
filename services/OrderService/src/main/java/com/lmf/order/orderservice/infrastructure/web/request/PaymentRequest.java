package com.lmf.order.orderservice.infrastructure.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(

        @NotBlank(message = "Payment method is required") String paymentMethod,

        @NotNull(message = "Installments is required") Integer installments,

        @NotNull(message = "amount is required") BigDecimal amount) {
}
