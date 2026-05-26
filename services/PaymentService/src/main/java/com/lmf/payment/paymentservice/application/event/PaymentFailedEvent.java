package com.lmf.payment.paymentservice.application.event;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentFailedEvent(

        UUID paymentId,

        UUID orderId,

        UUID customerId,

        BigDecimal amount,

        String currency,

        PaymentMethod paymentMethod,

        String failureReason,

        String gatewayStatus,

        OffsetDateTime failedAt

) {
}
