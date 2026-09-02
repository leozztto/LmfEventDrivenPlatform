package com.lmf.platform.messaging;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Linha do Transactional Outbox. Gravada na mesma transação da mudança de estado do agregado; o
 * {@link OutboxRelay} publica no Kafka de forma assíncrona e confiável.
 */
@Getter
@Entity
@Table(name = "outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    static final int MAX_RETRIES = 3;

    @Id
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "outbox_status", nullable = false)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "error_message")
    private String errorMessage;

    public OutboxEvent(UUID aggregateId, String aggregateType, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.retryCount = 0;
    }

    void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
    }

    void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
    }

    void markFailed(String errorMessage) {
        this.retryCount++;
        this.errorMessage = truncate(errorMessage);
        this.status = retryCount >= MAX_RETRIES ? OutboxStatus.DLT : OutboxStatus.FAILED;
    }

    void markPendingRetry() {
        this.status = OutboxStatus.PENDING;
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 300 ? value.substring(0, 300) : value;
    }
}
