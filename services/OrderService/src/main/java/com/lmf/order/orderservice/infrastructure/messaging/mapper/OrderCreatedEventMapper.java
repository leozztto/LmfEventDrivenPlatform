package com.lmf.order.orderservice.infrastructure.messaging.mapper;

import com.lmf.order.orderservice.domain.event.OrderCreatedEvent;
import com.lmf.order.orderservice.domain.model.order.Order;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class OrderCreatedEventMapper {

    public OrderCreatedEvent toEvent(Order order) {

        return new OrderCreatedEvent(UUID.randomUUID(), "ORDER_CREATED", "v1", OffsetDateTime.now(), order.getId(), order.getOrderStatus().name(), order.getTotalAmount(), order.getCustomerInfo(), order.getShippingAddress(), order.getPaymentInfo(), order.getOrderItems());
    }
}
