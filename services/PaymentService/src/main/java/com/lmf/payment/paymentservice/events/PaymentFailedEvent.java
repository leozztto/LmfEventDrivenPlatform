package com.lmf.payment.paymentservice.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentFailedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID orderId,

        String reason

) implements EventMessage {
}
