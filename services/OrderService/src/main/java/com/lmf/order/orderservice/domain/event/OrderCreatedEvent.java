package com.lmf.order.orderservice.domain.event;

import com.lmf.order.orderservice.domain.model.customer.CustomerInfo;
import com.lmf.order.orderservice.domain.model.order.OrderItem;
import com.lmf.order.orderservice.domain.model.payment.PaymentInfo;
import com.lmf.order.orderservice.domain.model.customer.ShippingAddress;

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
