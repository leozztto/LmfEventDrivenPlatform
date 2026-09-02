package com.lmf.platform.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Publicado no tópico {@code inventory.reservation.failed} pelo InventoryService quando a reserva de
 * estoque do pedido não pôde ser concluída. Consumido pelo OrderService para cancelar o pedido.
 */
public record InventoryReservationFailedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID orderId,

        String reason

) implements EventMessage {

    public static final String TYPE = "INVENTORY_RESERVATION_FAILED";
}
