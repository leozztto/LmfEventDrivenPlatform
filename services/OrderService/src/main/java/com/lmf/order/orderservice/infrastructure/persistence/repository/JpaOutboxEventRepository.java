package com.lmf.order.orderservice.infrastructure.persistence.repository;

import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaOutboxEventRepository
        extends JpaRepository<OutboxEventEntity, UUID> {
}