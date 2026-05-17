package com.lmf.order.orderservice.domain.repository;

import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;

public interface OutboxEventRepository {

    void save(OutboxEventEntity outboxEventEntity);

}
