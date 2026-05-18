package com.lmf.order.orderservice.infrastructure.persistence.entity;

import com.lmf.order.orderservice.domain.model.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus outboxStatus;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public OutboxEventEntity(UUID aggregateId, String aggregateType, String eventType, String payload, OutboxStatus outboxStatus) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.outboxStatus = outboxStatus;
        this.createdAt = OffsetDateTime.now();
    }

    public void markAsProcessing() {
        this.outboxStatus = OutboxStatus.PROCESSING;
    }

    public void markAsPublished() {
        this.outboxStatus = OutboxStatus.PUBLISHED;
    }

    public void markAsFailed() {
        this.outboxStatus = OutboxStatus.FAILED;
    }
}
