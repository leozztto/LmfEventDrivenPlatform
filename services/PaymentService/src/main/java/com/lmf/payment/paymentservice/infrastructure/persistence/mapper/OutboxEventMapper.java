package com.lmf.payment.paymentservice.infrastructure.persistence.mapper;

import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxEvent;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventMapper {

    public static OutboxEventEntity toEntity(OutboxEvent outboxEvent) {

        OutboxEventEntity outboxEventEntity = new OutboxEventEntity(outboxEvent.aggregateId(), outboxEvent.aggregateType(), outboxEvent.eventType(), outboxEvent.payload(), outboxEvent.outboxStatus());

        return outboxEventEntity;
    }
}