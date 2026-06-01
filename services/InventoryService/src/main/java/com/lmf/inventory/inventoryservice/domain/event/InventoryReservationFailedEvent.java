package com.lmf.inventory.inventoryservice.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryReservationFailedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID orderId,

        UUID productId,

        String reason) {
}
