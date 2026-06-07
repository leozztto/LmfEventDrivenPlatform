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

        OutboxEventEntity entity = new OutboxEventEntity(aggregateId, "PRODUCT", "PRODUCT_CREATED", "{\"id\":1}", OutboxStatus.PENDING);

        assertThat(entity.getId()).isNotNull();

        assertThat(entity.getAggregateId()).isEqualTo(aggregateId);

        assertThat(entity.getAggregateType()).isEqualTo("PRODUCT");

        assertThat(entity.getEventType()).isEqualTo("PRODUCT_CREATED");

        assertThat(entity.getPayload()).isEqualTo("{\"id\":1}");

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);

        assertThat(entity.getRetryCount()).isZero();

        assertThat(entity.getCreatedAt()).isNotNull();

        assertThat(entity.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should mark event as processing")
    void shouldMarkEventAsProcessing() {

        OutboxEventEntity entity = createEntity();

        entity.markAsProcessing();

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.PROCESSING);
    }

    @Test
    @DisplayName("Should mark event as published")
    void shouldMarkEventAsPublished() {

        OutboxEventEntity entity = createEntity();

        entity.markAsPublished();

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should mark event as failed on first retry")
    void shouldMarkEventAsFailedOnFirstRetry() {

        OutboxEventEntity entity = createEntity();

        entity.markAsFailed("Kafka unavailable");

        assertThat(entity.getRetryCount()).isEqualTo(1);

        assertThat(entity.getErrorMessage()).isEqualTo("Kafka unavailable");

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
    }

    @Test
    @DisplayName("Should mark event as failed on second retry")
    void shouldMarkEventAsFailedOnSecondRetry() {

        OutboxEventEntity entity = createEntity();

        entity.markAsFailed("error 1");
        entity.markAsFailed("error 2");

        assertThat(entity.getRetryCount()).isEqualTo(2);

        assertThat(entity.getErrorMessage()).isEqualTo("error 2");

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
    }

    @Test
    @DisplayName("Should move event to DLT after third retry")
    void shouldMoveEventToDltAfterThirdRetry() {

        OutboxEventEntity entity = createEntity();

        entity.markAsFailed("error 1");
        entity.markAsFailed("error 2");
        entity.markAsFailed("error 3");

        assertThat(entity.getRetryCount()).isEqualTo(3);

        assertThat(entity.getErrorMessage()).isEqualTo("error 3");

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.DLT);
    }

    @Test
    @DisplayName("Should keep DLT status after additional failures")
    void shouldKeepDltStatusAfterAdditionalFailures() {

        OutboxEventEntity entity = createEntity();

        entity.markAsFailed("error 1");
        entity.markAsFailed("error 2");
        entity.markAsFailed("error 3");
        entity.markAsFailed("error 4");

        assertThat(entity.getRetryCount()).isEqualTo(4);

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.DLT);

        assertThat(entity.getErrorMessage()).isEqualTo("error 4");
    }

    @Test
    @DisplayName("Should mark event as pending retry")
    void shouldMarkEventAsPendingRetry() {

        OutboxEventEntity entity = createEntity();

        entity.markAsFailed("temporary error");

        entity.markAsPendingRetry();

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);

        assertThat(entity.getRetryCount()).isEqualTo(1);

        assertThat(entity.getErrorMessage()).isEqualTo("temporary error");
    }

    @Test
    @DisplayName("Should transition processing failed pending retry")
    void shouldTransitionProcessingFailedPendingRetry() {

        OutboxEventEntity entity = createEntity();

        entity.markAsProcessing();

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.PROCESSING);

        entity.markAsFailed("temporary error");

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);

        entity.markAsPendingRetry();

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("Should transition processing published")
    void shouldTransitionProcessingPublished() {

        OutboxEventEntity entity = createEntity();

        entity.markAsProcessing();

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.PROCESSING);

        entity.markAsPublished();

        assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    private OutboxEventEntity createEntity() {

        return new OutboxEventEntity(UUID.randomUUID(), "PRODUCT", "PRODUCT_CREATED", "{\"id\":1}", OutboxStatus.PENDING);
    }
}