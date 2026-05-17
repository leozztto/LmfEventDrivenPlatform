package com.lmf.order.orderservice.domain.repository;

import com.lmf.order.orderservice.domain.model.OutboxStatus;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;

import java.util.List;

public interface OutboxEventRepository {

    void save(OutboxEventEntity outboxEventEntity);

    List<OutboxEventEntity> findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus outboxStatus);

    void update(OutboxEventEntity outboxEventEntity);
}
