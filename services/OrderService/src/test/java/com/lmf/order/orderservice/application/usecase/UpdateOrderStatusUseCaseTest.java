package com.lmf.order.orderservice.application.usecase;

import com.lmf.order.orderservice.domain.exception.OrderNotFoundException;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.model.order.OrderStatus;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @Test
    void shouldApprovePaymentWhenOrderIsPending() {

        Order order = TestDataFactory.createOrder();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        updateOrderStatusUseCase.approvePayment(order.getId());

        assertEquals(OrderStatus.PAYMENT_APPROVED, order.getOrderStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectPaymentWhenOrderIsPending() {

        Order order = TestDataFactory.createOrder();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        updateOrderStatusUseCase.rejectPayment(order.getId());

        assertEquals(OrderStatus.PAYMENT_REJECTED, order.getOrderStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldCancelForInventoryFailureWhenOrderIsPending() {

        Order order = TestDataFactory.createOrder();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        updateOrderStatusUseCase.cancelForInventoryFailure(order.getId());

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectForFraudWhenOrderIsPending() {

        Order order = TestDataFactory.createOrder();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        updateOrderStatusUseCase.rejectForFraud(order.getId());

        assertEquals(OrderStatus.FRAUD_REJECTED, order.getOrderStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldIgnoreFraudRejectionWhenOrderIsNoLongerPending() {

        Order order = TestDataFactory.createOrder();
        order.approvePayment();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        updateOrderStatusUseCase.rejectForFraud(order.getId());

        assertEquals(OrderStatus.PAYMENT_APPROVED, order.getOrderStatus());
        verify(orderRepository, never()).save(order);
    }

    @Test
    void shouldThrowOrderNotFoundWhenOrderDoesNotExist() {

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateOrderStatusUseCase.rejectForFraud(orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
