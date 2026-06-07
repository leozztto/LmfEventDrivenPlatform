package com.lmf.payment.paymentservice.domain.model.event;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;

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

        PaymentStatus paymentStatus,

        String provider,

        String transactionId,

        String gatewayStatus) {
}