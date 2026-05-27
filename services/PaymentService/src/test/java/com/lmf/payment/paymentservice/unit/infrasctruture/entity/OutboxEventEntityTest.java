package com.lmf.payment.paymentservice.unit.infrasctruture.entity;

import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventEntityTest {

    @Test
    @DisplayName("Should create outbox event with default values")
    void shouldCreateOutboxEventWithDefaultValues() {

        UUID aggregateId = UUID.randomUUID();

        OutboxEventEntity outboxEventEntity = new OutboxEventEntity(aggregateId, "PAYMENT", "PAYMENT_CREATED", """
                {
                  "paymentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "status": "APPROVED"
                }
                """, OutboxStatus.PENDING);

        assertNotNull(outboxEventEntity.getId());
        assertEquals(aggregateId, outboxEventEntity.getAggregateId());
        assertEquals("PAYMENT", outboxEventEntity.getAggregateType());
        assertEquals("PAYMENT_CREATED", outboxEventEntity.getEventType());
        assertEquals(OutboxStatus.PENDING, outboxEventEntity.getOutboxStatus());
        assertEquals(0, outboxEventEntity.getRetryCount());
        assertNull(outboxEventEntity.getErrorMessage());
        assertNotNull(outboxEventEntity.getCreatedAt());
    }

    @Test
    @DisplayName("Should mark outbox event as processing")
    void shouldMarkOutboxEventAsProcessing() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsProcessing();

        assertEquals(OutboxStatus.PROCESSING, outboxEventEntity.getOutboxStatus());
    }

    @Test
    @DisplayName("Should mark outbox event as published")
    void shouldMarkOutboxEventAsPublished() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsPublished();

        assertEquals(OutboxStatus.PUBLISHED, outboxEventEntity.getOutboxStatus());
    }

    @Test
    @DisplayName("Should mark outbox event as failed")
    void shouldMarkOutboxEventAsFailed() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsFailed("Kafka unavailable");

        assertEquals(1, outboxEventEntity.getRetryCount());
        assertEquals("Kafka unavailable", outboxEventEntity.getErrorMessage());
        assertEquals(OutboxStatus.FAILED, outboxEventEntity.getOutboxStatus());
    }

    @Test
    @DisplayName("Should move outbox event to DLT after third failure")
    void shouldMoveOutboxEventToDltAfterThirdFailure() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsFailed("error 1");
        outboxEventEntity.markAsFailed("error 2");
        outboxEventEntity.markAsFailed("error 3");

        assertEquals(3, outboxEventEntity.getRetryCount());
        assertEquals("error 3", outboxEventEntity.getErrorMessage());
        assertEquals(OutboxStatus.DLT, outboxEventEntity.getOutboxStatus());
    }

    @Test
    @DisplayName("Should mark outbox event as pending retry")
    void shouldMarkOutboxEventAsPendingRetry() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsFailed("temporary error");

        outboxEventEntity.markAsPendingRetry();

        assertEquals(OutboxStatus.PENDING, outboxEventEntity.getOutboxStatus());
    }

    private OutboxEventEntity createEntity() {

        return new OutboxEventEntity(UUID.randomUUID(), "PAYMENT", "PAYMENT_CREATED", """
                {
                  "paymentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "orderId": "7fa85f64-5717-4562-b3fc-2c963f66afb1",
                  "status": "APPROVED",
                  "amount": 299.90
                }
                """, OutboxStatus.PENDING);
    }
}