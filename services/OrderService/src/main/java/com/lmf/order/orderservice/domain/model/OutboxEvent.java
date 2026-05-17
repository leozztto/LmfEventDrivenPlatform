package com.lmf.order.orderservice.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        UUID aggregateId,
        String aggregateType,
        String eventType,
        String payload,
        OutboxStatus status,
        OffsetDateTime createdAt
) {
}
