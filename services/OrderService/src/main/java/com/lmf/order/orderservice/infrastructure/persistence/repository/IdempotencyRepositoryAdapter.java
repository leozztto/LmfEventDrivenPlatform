package com.lmf.order.orderservice.infrastructure.persistence.repository;

import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IdempotencyRepositoryAdapter {

    private final SpringDataIdempotencyRepository repository;

    public Optional<IdempotencyEntity> findByKey(String key) {
        return repository.findByKey(key);
    }

    public void save(IdempotencyEntity entity) {
        repository.save(entity);
    }
}
