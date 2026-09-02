package com.lmf.order.orderservice.infrastructure.messaging.mapper;

import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderCreatedEventMapperTest {

    private final OrderCreatedEventMapper orderCreatedEventMapper = new OrderCreatedEventMapper();

    @Test
    void shouldMapOrderCreatedEventSuccessfully() {

        Order order = TestDataFactory.createOrder();

        OrderCreatedEvent event = orderCreatedEventMapper.toEvent(order);

        assertEquals("ORDER_CREATED", event.eventType());

        assertEquals(order.getId(), event.orderId());

        assertEquals(order.getCustomerInfo().getEmail(), event.customer().email());
    }
}