package com.lmf.order.orderservice.infrastructure.persistence.repository;

import com.lmf.order.orderservice.domain.repository.IdempotencyStore;
import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class IdempotencyRepositoryAdapter implements IdempotencyStore {

    private final SpringDataIdempotencyRepository repository;

    @Override
    public Optional<UUID> findOrderIdByKey(String idempotencyKey) {

        return repository.findByKey(idempotencyKey).map(IdempotencyEntity::getOrderId);
    }

    @Override
    public void reserve(String idempotencyKey, UUID orderId) {

        repository.saveAndFlush(new IdempotencyEntity(idempotencyKey, orderId));
    }
}
