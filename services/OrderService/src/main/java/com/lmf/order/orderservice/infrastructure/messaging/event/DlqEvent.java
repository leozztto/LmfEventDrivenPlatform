package com.lmf.order.orderservice.infrastructure.messaging.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DlqEvent(

        UUID eventId,

        UUID aggregateId,

        String eventType,

        String payload,

        String errorMessage,

        Integer retryCount,

        OffsetDateTime failedAt) {
}
