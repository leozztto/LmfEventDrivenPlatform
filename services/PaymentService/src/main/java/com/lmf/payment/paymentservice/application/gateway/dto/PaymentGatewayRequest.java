package com.lmf.payment.paymentservice.application.gateway.dto;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentGatewayRequest(

        UUID paymentId,

        UUID orderId,

        UUID customerId,

        BigDecimal amount,

        String currency,

        PaymentMethod paymentMethod,

        Integer installments
) {
}
