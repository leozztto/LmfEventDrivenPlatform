package com.lmf.platform.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Publicado no tópico {@code inventory.reserved} pelo InventoryService quando todos os itens do
 * pedido foram reservados. É emitido uma única vez por pedido e carrega o que o PaymentService
 * precisa para processar o pagamento.
 */
public record InventoryReservedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID orderId,

        UUID customerId,

        BigDecimal totalAmount,

        PaymentInfo payment,

        List<ReservedItem> items

) implements EventMessage {

    public static final String TYPE = "INVENTORY_RESERVED";
}
