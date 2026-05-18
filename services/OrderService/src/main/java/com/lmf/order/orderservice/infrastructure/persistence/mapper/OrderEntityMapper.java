package com.lmf.order.orderservice.infrastructure.persistence.mapper;

import com.lmf.order.orderservice.domain.model.Order;
import com.lmf.order.orderservice.domain.model.OrderItem;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OrderEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import java.util.UUID;

@Component
public class OrderEntityMapper {

    public OrderEntity toEntity(Order order) {

        OrderEntity entity = new OrderEntity(order.getId(), order.getCustomerId(), order.getOrderStatus(), order.getTotalAmount(), order.getCreatedAt());

        order.getOrderItems().stream().map(this::toItemEntity).forEach(entity::addItem);

        return entity;
    }

    private OrderItemEntity toItemEntity(OrderItem item) {

        return new OrderItemEntity(UUID.randomUUID(), item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal());
    }

    public Order toDomain(OrderEntity orderEntity) {

        var orderItems = orderEntity.getOrderItemsEntities().stream().map(item -> new OrderItem(item.getProductId(), item.getQuantity(), item.getUnitPrice())).toList();

        return new Order(orderEntity.getId(), orderEntity.getCustomerId(), orderItems, orderEntity.getOrderStatus(), orderEntity.getCreatedAt());
    }
}