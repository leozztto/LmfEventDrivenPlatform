package com.lmf.inventory.inventoryservice.domain.event;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryReservedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID orderId,

        List<ReservedItem> items

) implements EventMessage {
}
