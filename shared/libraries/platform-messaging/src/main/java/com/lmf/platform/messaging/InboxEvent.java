package com.lmf.platform.messaging;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro do Inbox Pattern. Um evento só é considerado duplicado quando já está {@code PROCESSED} —
 * uma tentativa anterior que falhou (e teve rollback) não bloqueia o reprocessamento.
 */
@Getter
@Entity
@Table(name = "inbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboxEvent {

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
    @Column(name = "inbox_status", nullable = false)
    private InboxStatus status;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    public InboxEvent(String eventId, UUID aggregateId, String eventType) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.status = InboxStatus.RECEIVED;
        this.receivedAt = OffsetDateTime.now();
    }

    void markProcessed() {
        this.status = InboxStatus.PROCESSED;
        this.processedAt = OffsetDateTime.now();
    }

    void markFailed(String reason) {
        this.status = InboxStatus.FAILED;
        this.failureReason = reason;
    }
}
