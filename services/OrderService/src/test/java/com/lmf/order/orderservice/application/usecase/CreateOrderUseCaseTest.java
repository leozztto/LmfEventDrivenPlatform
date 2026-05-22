package com.lmf.order.orderservice.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.mapper.CreateOrderCommandMapper;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.exception.OrderNotFoundException;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.messaging.mapper.OrderCreatedEventMapper;
import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.order.orderservice.infrastructure.persistence.repository.IdempotencyRepositoryAdapter;
import com.lmf.order.orderservice.infrastructure.persistence.repository.SpringDataIdempotencyRepository;
import com.lmf.order.orderservice.infrastructure.persistence.repository.SpringDataOrderRepository;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    @Mock
    SpringDataIdempotencyRepository springDataIdempotencyRepository;

    @Mock
    SpringDataOrderRepository springDataOrderRepository;

    @BeforeEach
    void setup() {
        springDataOrderRepository.deleteAll();
        springDataIdempotencyRepository.deleteAll();
    }

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

    @Test
    void shouldCreateOrderSuccessfullyPendingPayment() throws Exception {

        String idempotencyKey = "testIntegration";

        CreateOrderCommand command = TestDataFactory.createCommand();

        Order order = TestDataFactory.createOrder();

        when(idempotencyRepositoryAdapter.findByKey(idempotencyKey)).thenReturn(Optional.empty());

        when(objectMapper.writeValueAsString(any())).thenReturn("{json}");

        when(createOrderCommandMapper.toDomain(any(CreateOrderCommand.class))).thenReturn(order);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        CreateOrderResult createOrderResult = createOrderUseCase.execute(command);

        assertThat(createOrderResult).isNotNull();

        assertThat(createOrderResult.status()).isEqualTo("PENDING_PAYMENT");

        verify(orderRepository).save(any(Order.class));

        verify(idempotencyRepositoryAdapter).save(any(IdempotencyEntity.class));

        verify(outboxEventRepository).save(any(OutboxEventEntity.class));
    }

    @Test
    void shouldReturnExistingOrderWhenIdempotencyKeyExists() {

        UUID orderId = UUID.randomUUID();
        String idempotencyKey = "testIntegration";

        CreateOrderCommand command = TestDataFactory.createCommand();

        Order order = TestDataFactory.createOrder();

        when(idempotencyRepositoryAdapter.findByKey(idempotencyKey)).thenReturn(Optional.of(new IdempotencyEntity(idempotencyKey, orderId)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        CreateOrderResult result = createOrderUseCase.execute(command);

        assertThat(result).isNotNull();

        verify(orderRepository, never()).save(any());

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSerializationFails() throws Exception {

        String idempotencyKey = "testIntegration";
        UUID customerUUID = UUID.randomUUID();
        UUID productUUID = UUID.randomUUID();

        CreateOrderCommand command = TestDataFactory.createCommand();

        Order order = TestDataFactory.createOrder();

        when(idempotencyRepositoryAdapter.findByKey(any())).thenReturn(Optional.empty());

        when(createOrderCommandMapper.toDomain(any(CreateOrderCommand.class))).thenReturn(order);

        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThatThrownBy(() -> createOrderUseCase.execute(command)).isInstanceOf(RuntimeException.class).hasMessage("Failed to create outbox event");
    }

    @Test
    void shouldThrowOrderNotFoundException() {

        String idempotencyKey = "testIntegration";
        UUID customerUUID = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID productUUID = UUID.randomUUID();

        CreateOrderCommand command = TestDataFactory.createCommand();

        when(idempotencyRepositoryAdapter.findByKey(any())).thenReturn(Optional.of(new IdempotencyEntity("idem-key", orderId)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createOrderUseCase.execute(command)).isInstanceOf(OrderNotFoundException.class);
    }
}
