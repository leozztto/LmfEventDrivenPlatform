package com.lmf.order.orderservice.infrastructure.persistence.mapper;

import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OrderEntity;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderEntityMapperTest {

    private final OrderEntityMapper orderEntityMapper = new OrderEntityMapper();

    @Test
    void shouldMapOrderToEntitySuccessfully() {

        Order order = TestDataFactory.createOrder();

        OrderEntity entity = orderEntityMapper.toEntity(order);

        assertEquals(order.getId(), entity.getId());

        assertEquals(order.getCustomerInfo().getName(), entity.getCustomer().getName());
    }

    @Test
    void shouldMapEntityToDomainSuccessfully() {

        Order order = TestDataFactory.createOrder();

        OrderEntity entity = orderEntityMapper.toEntity(order);

        Order mapped = orderEntityMapper.toDomain(entity);

        assertEquals(order.getId(), mapped.getId());

        assertEquals(order.getTotalAmount(), mapped.getTotalAmount());
    }
}
