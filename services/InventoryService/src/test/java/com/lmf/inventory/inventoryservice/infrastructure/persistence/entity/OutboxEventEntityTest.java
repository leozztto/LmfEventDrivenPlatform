package com.lmf.inventory.inventoryservice.infrastructure.persistence.entity;

import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventEntityTest {

    @Test
    @DisplayName("Should create entity with default values")
    void shouldCreateEntityWithDefaultValues() {

        UUID aggregateId = UUID.randomUUID();

        OutboxEventEntity outboxEventEntity = new OutboxEventEntity(aggregateId, "PRODUCT", "PRODUCT_CREATED", "{\"id\":1}", OutboxStatus.PENDING);

        assertThat(outboxEventEntity.getId()).isNotNull();

        assertThat(outboxEventEntity.getAggregateId()).isEqualTo(aggregateId);

        assertThat(outboxEventEntity.getAggregateType()).isEqualTo("PRODUCT");

        assertThat(outboxEventEntity.getEventType()).isEqualTo("PRODUCT_CREATED");

        assertThat(outboxEventEntity.getPayload()).isEqualTo("{\"id\":1}");

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);

        assertThat(outboxEventEntity.getRetryCount()).isZero();

        assertThat(outboxEventEntity.getCreatedAt()).isNotNull();

        assertThat(outboxEventEntity.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should mark event as processing")
    void shouldMarkEventAsProcessing() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsProcessing();

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PROCESSING);
    }

    @Test
    @DisplayName("Should mark event as published")
    void shouldMarkEventAsPublished() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsPublished();

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should mark event as failed on first retry")
    void shouldMarkEventAsFailedOnFirstRetry() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsFailed("Kafka unavailable");

        assertThat(outboxEventEntity.getRetryCount()).isEqualTo(1);

        assertThat(outboxEventEntity.getErrorMessage()).isEqualTo("Kafka unavailable");

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
    }

    @Test
    @DisplayName("Should mark event as failed on second retry")
    void shouldMarkEventAsFailedOnSecondRetry() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsFailed("error 1");
        outboxEventEntity.markAsFailed("error 2");

        assertThat(outboxEventEntity.getRetryCount()).isEqualTo(2);

        assertThat(outboxEventEntity.getErrorMessage()).isEqualTo("error 2");

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
    }

    @Test
    @DisplayName("Should move event to DLT after third retry")
    void shouldMoveEventToDltAfterThirdRetry() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsFailed("error 1");
        outboxEventEntity.markAsFailed("error 2");
        outboxEventEntity.markAsFailed("error 3");

        assertThat(outboxEventEntity.getRetryCount()).isEqualTo(3);

        assertThat(outboxEventEntity.getErrorMessage()).isEqualTo("error 3");

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.DLT);
    }

    @Test
    @DisplayName("Should keep DLT status after additional failures")
    void shouldKeepDltStatusAfterAdditionalFailures() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsFailed("error 1");
        outboxEventEntity.markAsFailed("error 2");
        outboxEventEntity.markAsFailed("error 3");
        outboxEventEntity.markAsFailed("error 4");

        assertThat(outboxEventEntity.getRetryCount()).isEqualTo(4);

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.DLT);

        assertThat(outboxEventEntity.getErrorMessage()).isEqualTo("error 4");
    }

    @Test
    @DisplayName("Should mark event as pending retry")
    void shouldMarkEventAsPendingRetry() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsFailed("temporary error");

        outboxEventEntity.markAsPendingRetry();

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);

        assertThat(outboxEventEntity.getRetryCount()).isEqualTo(1);

        assertThat(outboxEventEntity.getErrorMessage()).isEqualTo("temporary error");
    }

    @Test
    @DisplayName("Should transition processing failed pending retry")
    void shouldTransitionProcessingFailedPendingRetry() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsProcessing();

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PROCESSING);

        outboxEventEntity.markAsFailed("temporary error");

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);

        outboxEventEntity.markAsPendingRetry();

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("Should transition processing published")
    void shouldTransitionProcessingPublished() {

        OutboxEventEntity outboxEventEntity = createEntity();

        outboxEventEntity.markAsProcessing();

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PROCESSING);

        outboxEventEntity.markAsPublished();

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    private OutboxEventEntity createEntity() {

        return new OutboxEventEntity(UUID.randomUUID(), "PRODUCT", "PRODUCT_CREATED", "{\"id\":1}", OutboxStatus.PENDING);
    }
}