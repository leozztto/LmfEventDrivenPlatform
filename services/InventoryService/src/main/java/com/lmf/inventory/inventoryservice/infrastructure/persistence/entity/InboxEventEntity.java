package com.lmf.inventory.inventoryservice.infrastructure.persistence.entity;

import com.lmf.inventory.inventoryservice.infrastructure.persistence.model.InboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "inbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InboxStatus inboxStatus;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    public InboxEventEntity(String eventId, UUID aggregateId, String eventType) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.inboxStatus = InboxStatus.RECEIVED;
        this.receivedAt = OffsetDateTime.now();
    }

    public void markProcessed() {
        this.inboxStatus = InboxStatus.PROCESSED;
        this.processedAt = OffsetDateTime.now();
    }

    public void markFailed(String reason) {
        this.inboxStatus = InboxStatus.FAILED;
        this.failureReason = reason;
    }
}

