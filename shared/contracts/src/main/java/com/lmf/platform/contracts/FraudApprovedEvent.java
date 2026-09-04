package com.lmf.platform.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Publicado no tópico {@code fraud.approved} pelo FraudService quando um pedido passa nas regras de
 * fraude. Consumido pelo InventoryService para iniciar a reserva de estoque — substitui
 * {@code order.created} como gatilho da reserva.
 */
public record FraudApprovedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID orderId,

        CustomerInfo customer,

        BigDecimal totalAmount,

        PaymentInfo payment,

        List<OrderItem> items

) implements EventMessage {

    public static final String TYPE = "FRAUD_APPROVED";
}
