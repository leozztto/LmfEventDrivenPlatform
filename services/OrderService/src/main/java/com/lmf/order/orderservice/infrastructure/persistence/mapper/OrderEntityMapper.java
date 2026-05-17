package com.lmf.order.orderservice.infrastructure.persistence.mapper;

import com.lmf.order.orderservice.domain.model.Order;
import com.lmf.order.orderservice.domain.model.OrderItem;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OrderEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OrderItemEntity;

import java.util.stream.Collectors;

public class OrderEntityMapper {

    public OrderEntity toEntity(Order order) {

        OrderEntity entity = new OrderEntity(
                order.getId(),
                order.getCustomerId(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );

        order.getOrderItems()
                .stream()
                .map(this::toItemEntity)
                .forEach(entity::addItem);

        return entity;
    }

    private OrderItemEntity toItemEntity(OrderItem item) {

        return new OrderItemEntity(
                java.util.UUID.randomUUID(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    public Order toDomain(OrderEntity entity) {

        return new Order(
                entity.getCustomerId(),

                entity.getOrderItemsEntities()
                        .stream()
                        .map(item -> new OrderItem(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
