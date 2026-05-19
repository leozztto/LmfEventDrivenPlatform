package com.lmf.order.orderservice.infrastructure.persistence.repository;

import com.lmf.order.orderservice.domain.model.Order;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OrderEntity;
import com.lmf.order.orderservice.infrastructure.persistence.mapper.OrderEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository springDataOrderRepository;
    private final OrderEntityMapper orderEntityMapper;

    @Override
    public Order save(Order order) {

        var entity = orderEntityMapper.toEntity(order);
        var savedEntity = springDataOrderRepository.save(entity);
        return orderEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {

        return springDataOrderRepository.findById(orderId).map(orderEntityMapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return springDataOrderRepository.findAll().stream().map(orderEntityMapper::toDomain).toList();
    }
}