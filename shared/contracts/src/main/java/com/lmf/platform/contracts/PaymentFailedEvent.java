package com.lmf.platform.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Publicado no tópico {@code payment.failed} pelo PaymentService. Consumido pelo OrderService
 * (fecha o pedido como {@code PAYMENT_REJECTED}) e pelo InventoryService (libera a reserva).
 */
public record PaymentFailedEvent(

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

        String failureReason,

        String gatewayStatus

) implements EventMessage {

    public static final String TYPE = "PAYMENT_FAILED";
}
