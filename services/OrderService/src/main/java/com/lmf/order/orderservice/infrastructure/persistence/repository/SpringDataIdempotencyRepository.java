package com.lmf.order.orderservice.infrastructure.persistence.repository;

import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataIdempotencyRepository extends JpaRepository<IdempotencyEntity, UUID> {

    Optional<IdempotencyEntity> findByKey(String key);
}
