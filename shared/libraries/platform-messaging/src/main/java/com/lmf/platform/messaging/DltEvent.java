package com.lmf.platform.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DltEvent(

        UUID eventId,

        UUID aggregateId,

        String eventType,

        String payload,

        String errorMessage,

        int retryCount,

        OffsetDateTime failedAt) {

    static DltEvent from(OutboxEvent outboxEvent) {
        return new DltEvent(outboxEvent.getId(), outboxEvent.getAggregateId(), outboxEvent.getEventType(),
                outboxEvent.getPayload(), outboxEvent.getErrorMessage(), outboxEvent.getRetryCount(), OffsetDateTime.now());
    }
}
