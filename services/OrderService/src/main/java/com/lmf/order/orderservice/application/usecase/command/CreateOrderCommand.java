package com.lmf.order.orderservice.application.usecase.command;

import com.lmf.order.orderservice.domain.model.payment.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(

        String idempotencyKey,

        CustomerCommand customer,

        ShippingAddressCommand shippingAddress,

        PaymentCommand payment,

        List<OrderItemCommand> items) {

    public record CustomerCommand(

            UUID customerId,

            String name,

            String email,

            String phone) {
    }

    public record ShippingAddressCommand(

            String street,

            String number,

            String city,

            String zipCode,

            String country) {
    }

    public record PaymentCommand(

            PaymentMethod paymentMethod,

            Integer installments,

            BigDecimal paidAmount) {
    }

    public record OrderItemCommand(

            UUID productId,

            Integer quantity,

            BigDecimal unitPrice) {
    }
}