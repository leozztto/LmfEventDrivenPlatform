package com.lmf.order.orderservice.domain.repository;

import com.lmf.order.orderservice.domain.model.Order;

public interface OrderRepository {

    Order save(Order order);

}
