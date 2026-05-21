package com.lmf.order.orderservice.domain.repository;

import com.lmf.order.orderservice.domain.model.order.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    List<Order> findAll();
}
