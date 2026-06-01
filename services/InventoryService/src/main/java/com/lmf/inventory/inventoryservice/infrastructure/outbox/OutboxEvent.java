package com.lmf.inventory.inventoryservice.infrastructure.outbox;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OutboxEvent(

        UUID id,

        UUID aggregateId,

        String aggregateType,

        String eventType,

        String payload,

        OutboxStatus outboxStatus,

        OffsetDateTime createdAt) {
}
