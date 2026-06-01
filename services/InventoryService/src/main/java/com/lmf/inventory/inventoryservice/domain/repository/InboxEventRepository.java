package com.lmf.inventory.inventoryservice.domain.repository;

import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.InboxEventEntity;

import java.util.Optional;

public interface InboxEventRepository {

    boolean existsByEventId(String eventId);

    InboxEventEntity save(InboxEventEntity inboxMessage);

    Optional<InboxEventEntity> findByEventId(String eventId);
}
