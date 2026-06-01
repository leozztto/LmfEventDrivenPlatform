package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.InboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataInboxEventRepository extends JpaRepository<InboxEventEntity, UUID> {

    boolean existsByEventId(String eventId);

    Optional<InboxEventEntity> findByEventId(String eventId);
}