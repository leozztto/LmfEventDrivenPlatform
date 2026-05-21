package com.lmf.order.orderservice.application.usecase.mapper;

import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.domain.model.customer.CustomerInfo;
import com.lmf.order.orderservice.domain.model.customer.ShippingAddress;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.model.order.OrderItem;
import com.lmf.order.orderservice.domain.model.payment.PaymentInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateOrderCommandMapper {

    public Order toDomain(CreateOrderCommand command) {

        List<OrderItem> items = command.items().stream().map(item -> new OrderItem(item.productId(), item.quantity(), item.unitPrice())).toList();

        CustomerInfo customerInfo = new CustomerInfo(command.customer().customerId(), command.customer().name(), command.customer().email(), command.customer().phone());

        ShippingAddress shippingAddress = new ShippingAddress(command.shippingAddress().street(), command.shippingAddress().number(), command.shippingAddress().city(), command.shippingAddress().zipCode(), command.shippingAddress().country());

        PaymentInfo paymentInfo = new PaymentInfo(command.payment().paymentMethod(), command.payment().installments(), command.payment().paidAmount());

        return new Order(customerInfo, shippingAddress, paymentInfo, items);
    }
}
