package com.lmf.order.orderservice.domain.model;

import com.lmf.order.orderservice.domain.exception.EmptyOrderException;
import com.lmf.order.orderservice.domain.exception.InvalidOrderStatusException;
import com.lmf.order.orderservice.domain.exception.InvalidQuantityException;
import com.lmf.order.orderservice.domain.exception.InvalidUnitPriceException;
import com.lmf.order.orderservice.domain.model.customer.CustomerInfo;
import com.lmf.order.orderservice.domain.model.customer.ShippingAddress;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.model.order.OrderItem;
import com.lmf.order.orderservice.domain.model.order.OrderStatus;
import com.lmf.order.orderservice.domain.model.payment.PaymentInfo;
import com.lmf.order.orderservice.domain.model.payment.PaymentMethod;
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

        Order order = createOrder(List.of(createItem(2, 100)));

        assertNotNull(order.getId());

        assertEquals(OrderStatus.PENDING_PAYMENT, order.getOrderStatus());
    }

    @Test
    @DisplayName("Should calculate total amount correctly")
    void shouldCalculateTotalAmountCorrectly() {

        Order order = createOrder(List.of(createItem(2, 100), createItem(1, 50)));

        assertEquals(BigDecimal.valueOf(250), order.getTotalAmount());
    }

    @Test
    @DisplayName("Should throw exception when order has no items")
    void shouldThrowExceptionWhenOrderHasNoItems() {

        assertThrows(EmptyOrderException.class, () -> createOrder(List.of()));
    }

    @Test
    @DisplayName("Should approve payment successfully")
    void shouldApprovePaymentSuccessfully() {

        Order order = createOrder(List.of(createItem(1, 100)));

        order.approvePayment();

        assertEquals(OrderStatus.PAYMENT_APPROVED, order.getOrderStatus());
    }

    @Test
    @DisplayName("Should throw exception when payment transition is invalid")
    void shouldThrowExceptionWhenPaymentTransitionIsInvalid() {

        Order order = createOrder(List.of(createItem(1, 100)));

        order.approvePayment();

        assertThrows(InvalidOrderStatusException.class, order::approvePayment);
    }

    @Test
    @DisplayName("Should reject payment successfully")
    void shouldRejectPaymentSuccessfully() {

        Order order = createOrder(List.of(createItem(1, 10)));

        order.rejectPayment();

        assertEquals(OrderStatus.PAYMENT_REJECTED, order.getOrderStatus());
    }

    @Test
    @DisplayName("Should throw exception when approving invalid status")
    void shouldThrowExceptionWhenApprovingInvalidStatus() {

        Order order = createOrder(List.of(createItem(1, 10)));

        order.approvePayment();

        assertThrows(InvalidOrderStatusException.class, order::approvePayment);
    }

    @Test
    @DisplayName("Should throw exception when creating empty order")
    void shouldThrowExceptionWhenCreatingEmptyOrder() {

        assertThrows(EmptyOrderException.class, () -> createOrder(List.of()));
    }

    @Test
    @DisplayName("Should create order with pending payment status")
    void shouldCreateOrderWithPendingPaymentStatus() {

        Order order = createOrder(List.of(createItem(1, 100)));

        assertEquals(OrderStatus.PENDING_PAYMENT, order.getOrderStatus());
    }

    @Test
    @DisplayName("Should set createdAt when creating order")
    void shouldSetCreatedAtWhenCreatingOrder() {

        Order order = createOrder(List.of(createItem(1, 100)));

        assertNotNull(order.getCreatedAt());
    }

    @Test
    @DisplayName("Should store customer info correctly")
    void shouldStoreCustomerInfoCorrectly() {

        Order order = createOrder(List.of(createItem(1, 100)));

        assertEquals("Leandro", order.getCustomerInfo().getName());

        assertEquals("leandro@email.com", order.getCustomerInfo().getEmail());
    }

    @Test
    @DisplayName("Should store payment info correctly")
    void shouldStorePaymentInfoCorrectly() {

        Order order = createOrder(List.of(createItem(1, 100)));

        assertEquals(PaymentMethod.PIX, order.getPaymentInfo().paymentMethod());

        assertEquals(3, order.getPaymentInfo().installments());
    }

    @Test
    @DisplayName("Should store shipping address correctly")
    void shouldStoreShippingAddressCorrectly() {

        Order order = createOrder(List.of(createItem(1, 100)));

        assertEquals("São Paulo", order.getShippingAddress().getCity());
    }

    @Test
    @DisplayName("Should calculate item subtotal correctly")
    void shouldCalculateItemSubtotalCorrectly() {

        OrderItem item = new OrderItem(UUID.randomUUID(), 2, BigDecimal.valueOf(150));

        assertEquals(BigDecimal.valueOf(300), item.getSubtotal());
    }

    @Test
    @DisplayName("Should throw exception for invalid quantity")
    void shouldThrowExceptionForInvalidQuantity() {

        assertThrows(InvalidQuantityException.class, () -> new OrderItem(UUID.randomUUID(), 0, BigDecimal.TEN));
    }

    @Test
    @DisplayName("Should throw exception for invalid unit price")
    void shouldThrowExceptionForInvalidUnitPrice() {

        assertThrows(InvalidUnitPriceException.class, () -> new OrderItem(UUID.randomUUID(), 1, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should throw exception when rejecting invalid status")
    void shouldThrowExceptionWhenRejectingInvalidStatus() {

        Order order = createOrder(List.of(createItem(1, 100)));

        order.rejectPayment();

        assertThrows(InvalidOrderStatusException.class, order::rejectPayment);
    }

    private Order createOrder(List<OrderItem> items) {

        return new Order(createCustomerInfo(), createShippingAddress(), createPaymentInfo(), items);
    }

    private CustomerInfo createCustomerInfo() {

        return new CustomerInfo(UUID.randomUUID(), "Leandro", "leandro@email.com", "11999999999");
    }

    private ShippingAddress createShippingAddress() {

        return new ShippingAddress("Rua XPTO", "100", "São Paulo", "01000000", "BR");
    }

    private PaymentInfo createPaymentInfo() {

        return new PaymentInfo(PaymentMethod.PIX, 3, new BigDecimal(100));
    }

    private OrderItem createItem(int quantity, int price) {

        return new OrderItem(UUID.randomUUID(), quantity, BigDecimal.valueOf(price));
    }
}
