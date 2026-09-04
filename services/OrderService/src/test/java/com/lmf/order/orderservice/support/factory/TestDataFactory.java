package com.lmf.order.orderservice.support.factory;

import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.domain.model.customer.CustomerInfo;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.model.order.OrderItem;
import com.lmf.order.orderservice.domain.model.payment.PaymentInfo;
import com.lmf.order.orderservice.domain.model.customer.ShippingAddress;
import com.lmf.order.orderservice.domain.model.payment.PaymentMethod;
import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import com.lmf.order.orderservice.infrastructure.web.request.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Order createOrder() {

        return new Order(createCustomerInfo(), createShippingAddress(), createPaymentInfo(), List.of(createOrderItem()));
    }

    public static OrderItem createOrderItem() {

        return new OrderItem(UUID.randomUUID(), 2, BigDecimal.valueOf(100));
    }

    public static CustomerInfo createCustomerInfo() {

        return new CustomerInfo(UUID.randomUUID(), "Leandro", "leandro@email.com", "11999999999");
    }

    public static ShippingAddress createShippingAddress() {

        return new ShippingAddress("Rua XPTO", "100", "São Paulo", "01000000", "BR");
    }

    public static PaymentInfo createPaymentInfo() {

        return new PaymentInfo(PaymentMethod.BOLETO, 3, new BigDecimal(150));
    }

    public static CreateOrderCommand createCommand() {

        return new CreateOrderCommand(

                "testIntegration",

                new CreateOrderCommand.CustomerCommand(UUID.randomUUID(), "Leandro", "leandro@email.com", "11999999999"),

                new CreateOrderCommand.ShippingAddressCommand("Rua XPTO", "100", "São Paulo", "01000000", "BR"),

                new CreateOrderCommand.PaymentCommand(PaymentMethod.CREDIT_CARD, 3, new BigDecimal(200)),

                List.of(new CreateOrderCommand.OrderItemCommand(UUID.randomUUID(), 2, BigDecimal.valueOf(100))));
    }

    public static IdempotencyEntity createIdempotencyEntity(UUID orderId) {

        return new IdempotencyEntity(UUID.randomUUID().toString(), orderId);
    }

    public static CreateOrderRequest createRequest() {

        CustomerRequest customer = new CustomerRequest(UUID.randomUUID(), "Leandro", "leandro@email.com", "11999999999");

        ShippingAddressRequest shipping = new ShippingAddressRequest("Rua XPTO", "100", "São Paulo", "01000000", "BR");

        PaymentRequest payment = new PaymentRequest("APPLE_PAY", 3, new BigDecimal("50"));

        OrderItemRequest item = new OrderItemRequest(UUID.randomUUID(), 2, BigDecimal.valueOf(100));

        return new CreateOrderRequest(customer, shipping, payment, List.of(item));
    }
}
