package com.lmf.payment.paymentservice.application.command;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.platform.contracts.EventMessage;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentCommand(

        UUID orderId,

        UUID eventId,

        String eventType,

        UUID customerId,

        BigDecimal amount,

        String currency,

        PaymentMethod paymentMethod,

        Integer installments

) implements EventMessage {
}