package com.lmf.order.orderservice.application.mapper;

import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.mapper.CreateOrderCommandMapper;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CreateOrderCommandMapperTest {

    private final CreateOrderCommandMapper createOrderCommandMapper = new CreateOrderCommandMapper();

    @Test
    void shouldMapCommandToDomainSuccessfully() {

        CreateOrderCommand command = TestDataFactory.createCommand();

        Order order = createOrderCommandMapper.toDomain(command);

        assertNotNull(order);

        assertEquals(command.customer().name(), order.getCustomerInfo().getName());

        assertEquals(command.payment().paymentMethod(), order.getPaymentInfo().paymentMethod());
    }
}
