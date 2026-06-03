package com.lmf.payment.paymentservice.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentProcessedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID orderId,

        UUID paymentId,

        BigDecimal amount

) implements EventMessage {
}
