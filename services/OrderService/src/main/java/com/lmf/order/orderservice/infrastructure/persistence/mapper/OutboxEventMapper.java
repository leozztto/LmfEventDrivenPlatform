package com.lmf.order.orderservice.infrastructure.persistence.mapper;

import com.lmf.order.orderservice.domain.model.outbox.OutboxEvent;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventMapper {

    public OutboxEventEntity toEntity(OutboxEvent event) {

        OutboxEventEntity entity = new OutboxEventEntity(event.aggregateId(), event.aggregateType(), event.eventType(), event.payload(), event.outboxStatus());

        return entity;
    }
}