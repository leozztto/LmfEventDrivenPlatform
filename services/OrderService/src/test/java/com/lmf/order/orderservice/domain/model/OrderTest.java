package com.lmf.order.orderservice.domain.model;

import com.lmf.order.orderservice.domain.exception.EmptyOrderException;
import com.lmf.order.orderservice.domain.exception.InvalidOrderStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {

        Order order = new Order(UUID.randomUUID(), List.of(createItem(2, 100)));

        assertNotNull(order.getId());

        assertEquals(OrderStatus.PENDING_PAYMENT, order.getOrderStatus());
    }

    @Test
    @DisplayName("Should calculate total amount correctly")
    void shouldCalculateTotalAmountCorrectly() {

        Order order = new Order(UUID.randomUUID(), List.of(createItem(2, 100), createItem(1, 50)));

        assertEquals(BigDecimal.valueOf(250), order.getTotalAmount());
    }

    @Test
    @DisplayName("Should throw exception when order has no items")
    void shouldThrowExceptionWhenOrderHasNoItems() {

        assertThrows(EmptyOrderException.class, () -> new Order(UUID.randomUUID(), List.of()));
    }

    @Test
    @DisplayName("Should approve payment successfully")
    void shouldApprovePaymentSuccessfully() {

        Order order = new Order(UUID.randomUUID(), List.of(createItem(1, 100)));

        order.approvePayment();

        assertEquals(OrderStatus.PAYMENT_APPROVED, order.getOrderStatus());
    }

    @Test
    @DisplayName("Should throw exception when payment transition is invalid")
    void shouldThrowExceptionWhenPaymentTransitionIsInvalid() {

        Order order = new Order(UUID.randomUUID(), List.of(createItem(1, 100)));

        order.approvePayment();

        assertThrows(InvalidOrderStatusException.class, order::approvePayment);
    }

    private OrderItem createItem(int quantity, int price) {

        return new OrderItem(UUID.randomUUID(), quantity, BigDecimal.valueOf(price));
    }
}
