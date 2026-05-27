package com.lmf.payment.paymentservice.domain.repository;

import com.lmf.payment.paymentservice.infrastructure.persistence.entity.InboxEventEntity;

import java.util.Optional;

public interface InboxEventRepository {

    boolean existsByEventId(String eventId);

    InboxEventEntity save(InboxEventEntity inboxMessage);

    Optional<InboxEventEntity> findByEventId(String eventId);
}
