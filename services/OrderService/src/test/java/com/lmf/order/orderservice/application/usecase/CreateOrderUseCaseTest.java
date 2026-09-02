package com.lmf.order.orderservice.application.usecase;

import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.mapper.CreateOrderCommandMapper;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.exception.OrderNotFoundException;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.repository.IdempotencyStore;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.infrastructure.messaging.OrderOutboxWriter;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private IdempotencyStore idempotencyStore;

    @Mock
    private OrderOutboxWriter orderOutboxWriter;

    @Mock
    private CreateOrderCommandMapper createOrderCommandMapper;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    @Test
    void shouldCreateOrderReservingIdempotencyKeyBeforePersistingAndWritingOutbox() {

        CreateOrderCommand command = TestDataFactory.createCommand();
        Order order = TestDataFactory.createOrder();

        when(idempotencyStore.findOrderIdByKey(command.idempotencyKey())).thenReturn(Optional.empty());
        when(createOrderCommandMapper.toDomain(command)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        CreateOrderResult result = createOrderUseCase.execute(command);

        assertNotNull(result);
        assertEquals(order.getId(), result.orderId());

        var inOrder = inOrder(idempotencyStore, orderRepository, orderOutboxWriter);
        inOrder.verify(idempotencyStore).reserve(command.idempotencyKey(), order.getId());
        inOrder.verify(orderRepository).save(order);
        inOrder.verify(orderOutboxWriter).writeOrderCreated(order);
    }

    @Test
    void shouldReturnExistingOrderWhenIdempotencyKeyAlreadyExists() {

        CreateOrderCommand command = TestDataFactory.createCommand();
        Order order = TestDataFactory.createOrder();

        when(idempotencyStore.findOrderIdByKey(command.idempotencyKey())).thenReturn(Optional.of(order.getId()));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        CreateOrderResult result = createOrderUseCase.execute(command);

        assertNotNull(result);
        assertEquals(order.getId(), result.orderId());

        verify(orderRepository, never()).save(any(Order.class));
        verify(idempotencyStore, never()).reserve(any(), any());
        verifyNoInteractions(orderOutboxWriter);
    }

    @Test
    void shouldPropagateSerializationFailureFromOutboxWriter() {

        CreateOrderCommand command = TestDataFactory.createCommand();
        Order order = TestDataFactory.createOrder();

        when(idempotencyStore.findOrderIdByKey(any())).thenReturn(Optional.empty());
        when(createOrderCommandMapper.toDomain(any(CreateOrderCommand.class))).thenReturn(order);
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Failed to serialize order created event"))
                .when(orderOutboxWriter).writeOrderCreated(order);

        assertThatThrownBy(() -> createOrderUseCase.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize");
    }

    @Test
    void shouldThrowOrderNotFoundWhenIdempotencyPointsToMissingOrder() {

        CreateOrderCommand command = TestDataFactory.createCommand();
        UUID orderId = UUID.randomUUID();

        when(idempotencyStore.findOrderIdByKey(any())).thenReturn(Optional.of(orderId));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createOrderUseCase.execute(command)).isInstanceOf(OrderNotFoundException.class);
    }
}
