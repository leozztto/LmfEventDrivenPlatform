package com.lmf.payment.paymentservice.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
}
