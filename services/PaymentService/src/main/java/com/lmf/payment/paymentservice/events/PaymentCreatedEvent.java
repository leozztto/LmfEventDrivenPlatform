package com.lmf.payment.paymentservice.events;

import com.lmf.payment.paymentservice.domain.PaymentMethod;
import com.lmf.payment.paymentservice.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentCreatedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID paymentId,

        UUID orderId,

        UUID customerId,

        BigDecimal amount,

        String currency,

        PaymentMethod paymentMethod,

        Integer installments,

        PaymentStatus status,

        String provider,

        String transactionId,

        String gatewayStatus) {
}