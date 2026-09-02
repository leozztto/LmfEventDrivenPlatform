package com.lmf.platform.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Publicado no tópico {@code payment.approved} pelo PaymentService. Consumido pelo OrderService
 * (fecha o pedido como {@code PAYMENT_APPROVED}) e pelo InventoryService (confirma a reserva).
 */
public record PaymentApprovedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID paymentId,

        UUID orderId,

        UUID customerId,

        BigDecimal amount,

        String currency,

        PaymentMethod paymentMethod,

        String transactionId,

        String provider

) implements EventMessage {

    public static final String TYPE = "PAYMENT_APPROVED";
}
