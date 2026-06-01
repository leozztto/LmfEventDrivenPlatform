package com.lmf.inventory.inventoryservice.domain.event;

import com.lmf.inventory.inventoryservice.domain.event.order.CustomerInfo;
import com.lmf.inventory.inventoryservice.domain.event.order.OrderItem;
import com.lmf.inventory.inventoryservice.domain.event.order.PaymentInfo;
import com.lmf.inventory.inventoryservice.domain.event.order.ShippingAddress;

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
