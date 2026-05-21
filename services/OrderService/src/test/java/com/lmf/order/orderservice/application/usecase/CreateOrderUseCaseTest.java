package com.lmf.order.orderservice.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.mapper.CreateOrderCommandMapper;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.messaging.mapper.OrderCreatedEventMapper;
import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.order.orderservice.infrastructure.persistence.repository.IdempotencyRepositoryAdapter;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private IdempotencyRepositoryAdapter idempotencyRepositoryAdapter;

    @Mock
    private CreateOrderCommandMapper createOrderCommandMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    OrderCreatedEventMapper orderCreatedEventMapper;

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        CreateOrderCommand command = TestDataFactory.createCommand();

        Order order = TestDataFactory.createOrder();

        when(createOrderCommandMapper.toDomain(command)).thenReturn(order);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"ORDER_CREATED\"}");

        CreateOrderResult result = createOrderUseCase.execute(command);

        assertNotNull(result);

        assertEquals(order.getId(), result.orderId());

        verify(orderRepository).save(any(Order.class));

        verify(outboxEventRepository).save(any(OutboxEventEntity.class));

        verify(idempotencyRepositoryAdapter).save(any(IdempotencyEntity.class));
    }

    @Test
    void shouldReturnExistingOrderWhenIdempotencyKeyAlreadyExists() {

        CreateOrderCommand command = TestDataFactory.createCommand();

        Order order = TestDataFactory.createOrder();

        IdempotencyEntity idempotency = TestDataFactory.createIdempotencyEntity(order.getId());

        when(idempotencyRepositoryAdapter.findByKey(command.idempotencyKey())).thenReturn(Optional.of(idempotency));

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        CreateOrderResult result = createOrderUseCase.execute(command);

        assertNotNull(result);

        assertEquals(order.getId(), result.orderId());

        verify(orderRepository, never()).save(any(Order.class));

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldCreateOutboxEvent() throws Exception {

        CreateOrderCommand command = TestDataFactory.createCommand();

        Order order = TestDataFactory.createOrder();

        when(createOrderCommandMapper.toDomain(command)).thenReturn(order);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"event\":\"ORDER_CREATED\"}");

        createOrderUseCase.execute(command);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(outboxEventRepository).save(captor.capture());

        OutboxEventEntity event = captor.getValue();

        assertEquals("ORDER_CREATED", event.getEventType());

        assertEquals(order.getId(), event.getAggregateId());
    }
}
