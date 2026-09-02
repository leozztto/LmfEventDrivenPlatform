package com.lmf.platform.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InboxEventRepository extends JpaRepository<InboxEvent, UUID> {

    Optional<InboxEvent> findByEventId(String eventId);
}
