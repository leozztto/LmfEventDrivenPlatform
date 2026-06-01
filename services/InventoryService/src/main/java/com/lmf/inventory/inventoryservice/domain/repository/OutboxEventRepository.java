package com.lmf.inventory.inventoryservice.domain.repository;

import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxEventRepository {

    void save(OutboxEventEntity outboxEventEntity);

    List<OutboxEventEntity> findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus outboxStatus);

    void update(OutboxEventEntity outboxEventEntity);

    List<OutboxEventEntity> findAll();

    Optional<OutboxEventEntity> findById(UUID id);
}
