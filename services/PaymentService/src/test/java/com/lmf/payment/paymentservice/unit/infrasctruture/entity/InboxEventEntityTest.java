package com.lmf.payment.paymentservice.unit.infrasctruture.entity;

import com.lmf.payment.paymentservice.infrastructure.inbox.InboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.InboxEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InboxEventEntityTest {

    @Test
    @DisplayName("Should create inbox event with default values")
    void shouldCreateInboxEventWithDefaultValues() {

        UUID aggregateId = UUID.randomUUID();

        InboxEventEntity inboxEventEntity = new InboxEventEntity("event-123", aggregateId, "ORDER_CREATED");

        assertEquals("event-123", inboxEventEntity.getEventId());
        assertEquals(aggregateId, inboxEventEntity.getAggregateId());
        assertEquals("ORDER_CREATED", inboxEventEntity.getEventType());
        assertEquals(InboxStatus.RECEIVED, inboxEventEntity.getInboxStatus());
        assertNotNull(inboxEventEntity.getReceivedAt());
        assertNull(inboxEventEntity.getProcessedAt());
        assertNull(inboxEventEntity.getFailureReason());
    }

    @Test
    @DisplayName("Should mark inbox event as processed")
    void shouldMarkInboxEventAsProcessed() {

        InboxEventEntity inboxEventEntity = createEntity();

        inboxEventEntity.markProcessed();

        assertEquals(InboxStatus.PROCESSED, inboxEventEntity.getInboxStatus());
        assertNotNull(inboxEventEntity.getProcessedAt());
    }

    @Test
    @DisplayName("Should mark inbox event as failed")
    void shouldMarkInboxEventAsFailed() {

        InboxEventEntity inboxEventEntity = createEntity();

        inboxEventEntity.markFailed("Processing error");

        assertEquals(InboxStatus.FAILED, inboxEventEntity.getInboxStatus());
        assertEquals("Processing error", inboxEventEntity.getFailureReason());
    }

    private InboxEventEntity createEntity() {

        return new InboxEventEntity("event-123", UUID.randomUUID(), "ORDER_CREATED");
    }
}