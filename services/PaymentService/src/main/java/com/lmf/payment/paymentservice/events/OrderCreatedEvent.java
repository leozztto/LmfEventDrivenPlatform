package com.lmf.payment.paymentservice.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

        List<OrderItem> items) {
}
