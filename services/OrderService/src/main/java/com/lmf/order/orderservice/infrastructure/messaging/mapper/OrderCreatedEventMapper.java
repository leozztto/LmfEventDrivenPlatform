package com.lmf.order.orderservice.infrastructure.messaging.mapper;

import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.platform.contracts.CustomerInfo;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.contracts.OrderItem;
import com.lmf.platform.contracts.PaymentInfo;
import com.lmf.platform.contracts.PaymentMethod;
import com.lmf.platform.contracts.ShippingAddress;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class OrderCreatedEventMapper {

    public OrderCreatedEvent toEvent(Order order) {

        CustomerInfo customer = new CustomerInfo(
                order.getCustomerInfo().getCustomerId(),
                order.getCustomerInfo().getName(),
                order.getCustomerInfo().getEmail(),
                order.getCustomerInfo().getPhone());

        ShippingAddress shippingAddress = new ShippingAddress(
                order.getShippingAddress().getStreet(),
                order.getShippingAddress().getNumber(),
                order.getShippingAddress().getCity(),
                order.getShippingAddress().getZipCode(),
                order.getShippingAddress().getCountry());

        PaymentInfo payment = new PaymentInfo(
                PaymentMethod.valueOf(order.getPaymentInfo().paymentMethod().name()),
                order.getPaymentInfo().installments(),
                order.getPaymentInfo().paidAmount());

        List<OrderItem> items = order.getOrderItems().stream()
                .map(item -> new OrderItem(item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()))
                .toList();

        return new OrderCreatedEvent(
                UUID.randomUUID(),
                OrderCreatedEvent.TYPE,
                "v1",
                OffsetDateTime.now(),
                order.getId(),
                order.getOrderStatus().name(),
                order.getTotalAmount(),
                customer,
                shippingAddress,
                payment,
                items);
    }
}
