package com.lmf.platform.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Publicado no tópico {@code order.created} pelo OrderService. Consumido pelo InventoryService para
 * iniciar a reserva de estoque.
 */
public record OrderCreatedEvent(

        UUID eventId,

        String eventType,

        String eventVersion,

        OffsetDateTime occurredAt,

        UUID orderId,

        String status,

        BigDecimal totalAmount,

        CustomerInfo customer,

        ShippingAddress shippingAddress,

        PaymentInfo payment,

        List<OrderItem> items

) implements EventMessage {

    public static final String TYPE = "ORDER_CREATED";
}
