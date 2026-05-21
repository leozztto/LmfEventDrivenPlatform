package com.lmf.order.orderservice.infrastructure.persistence.mapper;

import com.lmf.order.orderservice.domain.model.customer.CustomerInfo;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.model.order.OrderItem;
import com.lmf.order.orderservice.domain.model.payment.PaymentInfo;
import com.lmf.order.orderservice.domain.model.customer.ShippingAddress;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OrderEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OrderItemEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.embedded.CustomerEmbeddable;
import com.lmf.order.orderservice.infrastructure.persistence.entity.embedded.PaymentInfoEmbeddable;
import com.lmf.order.orderservice.infrastructure.persistence.entity.embedded.ShippingAddressEmbeddable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderEntityMapper {

    public OrderEntity toEntity(Order order) {

        CustomerEmbeddable customer = new CustomerEmbeddable(order.getCustomerInfo().getCustomerId(), order.getCustomerInfo().getName(), order.getCustomerInfo().getEmail(), order.getCustomerInfo().getPhone());

        ShippingAddressEmbeddable shippingAddress = new ShippingAddressEmbeddable(order.getShippingAddress().getStreet(), order.getShippingAddress().getNumber(), order.getShippingAddress().getCity(), order.getShippingAddress().getZipCode(), order.getShippingAddress().getCountry());

        PaymentInfoEmbeddable paymentInfo = new PaymentInfoEmbeddable(order.getPaymentInfo().paymentMethod(), order.getPaymentInfo().installments(), order.getPaymentInfo().paidAmount());

        OrderEntity entity = new OrderEntity(order.getId(), customer, shippingAddress, paymentInfo, order.getOrderStatus(), order.getTotalAmount(), order.getCreatedAt());

        order.getOrderItems().stream().map(this::toItemEntity).forEach(entity::addItem);

        return entity;
    }

    private OrderItemEntity toItemEntity(OrderItem item) {

        return new OrderItemEntity(UUID.randomUUID(), item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal());
    }

    public Order toDomain(OrderEntity orderEntity) {

        List<OrderItem> orderItems = orderEntity.getOrderItemsEntities().stream().map(item -> new OrderItem(item.getProductId(), item.getQuantity(), item.getUnitPrice())).toList();

        CustomerInfo customerInfo = new CustomerInfo(orderEntity.getCustomer().getCustomerId(), orderEntity.getCustomer().getName(), orderEntity.getCustomer().getEmail(), orderEntity.getCustomer().getPhone());

        ShippingAddress shippingAddress = new ShippingAddress(orderEntity.getShippingAddress().getStreet(), orderEntity.getShippingAddress().getNumber(), orderEntity.getShippingAddress().getCity(), orderEntity.getShippingAddress().getZipCode(), orderEntity.getShippingAddress().getCountry());

        PaymentInfo paymentInfo = new PaymentInfo(orderEntity.getPaymentInfo().getPaymentMethod(), orderEntity.getPaymentInfo().getInstallments(), orderEntity.getPaymentInfo().getPaidAmount());

        return new Order(orderEntity.getId(), customerInfo, shippingAddress, paymentInfo, orderItems, orderEntity.getOrderStatus(), orderEntity.getCreatedAt());
    }
}