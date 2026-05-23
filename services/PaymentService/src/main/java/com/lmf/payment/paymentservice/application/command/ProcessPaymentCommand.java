package com.lmf.payment.paymentservice.application.command;

import com.lmf.payment.paymentservice.domain.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentCommand(

        UUID orderId,

        UUID customerId,

        BigDecimal amount,

        String currency,

        PaymentMethod paymentMethod,

        Integer installments) {
}
