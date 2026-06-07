package com.lmf.inventory.inventoryservice.infrastructure.kafka.producer;

import com.lmf.inventory.inventoryservice.domain.repository.OutboxEventRepository;
import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private InventoryEventPublisher inventoryEventPublisher;

    @InjectMocks
    private OutboxEventProcessor outboxEventProcessor;

    private UUID aggregateId;

    @BeforeEach
    void setup() {

        aggregateId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should do nothing when no pending events exist")
    void shouldDoNothingWhenNoPendingEventsExist() {

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of());

        outboxEventProcessor.process();

        verify(outboxEventRepository).findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        verifyNoInteractions(inventoryEventPublisher);
    }

    @Test
    @DisplayName("Should publish event successfully")
    void shouldPublishEventSuccessfully() {

        OutboxEventEntity outboxEventEntity = createPendingEvent();

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        outboxEventProcessor.process();

        verify(outboxEventRepository, times(2)).update(outboxEventEntity);

        verify(inventoryEventPublisher).publish(KafkaTopics.INVENTORY_RESERVED, aggregateId.toString(), outboxEventEntity.toString());

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should process all pending events")
    void shouldProcessAllPendingEvents() {

        OutboxEventEntity outboxEventEntityFirst = createPendingEvent();

        OutboxEventEntity outboxEventEntitySecond = new OutboxEventEntity(UUID.randomUUID(), "ORDER", "INVENTORY_RESERVED", "{\"event\":\"2\"}", OutboxStatus.PENDING);

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntityFirst, outboxEventEntitySecond));

        outboxEventProcessor.process();

        verify(inventoryEventPublisher, times(2)).publish(anyString(), anyString(), anyString());

        assertThat(outboxEventEntityFirst.getOutboxStatus()).isEqualTo(OutboxStatus.PUBLISHED);

        assertThat(outboxEventEntitySecond.getOutboxStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should mark event as failed and return to pending retry")
    void shouldMarkEventAsFailedAndReturnToPendingRetry() {

        OutboxEventEntity outboxEventEntity = createPendingEvent();

        doThrow(new RuntimeException("Kafka unavailable")).when(inventoryEventPublisher).publish(anyString(), anyString(), anyString());

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        outboxEventProcessor.process();

        verify(outboxEventRepository).save(outboxEventEntity);

        assertThat(outboxEventEntity.getRetryCount()).isEqualTo(1);

        assertThat(outboxEventEntity.getErrorMessage()).isEqualTo("Kafka unavailable");

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("Should send event to DLT after third failure")
    void shouldSendEventToDltAfterThirdFailure() {

        OutboxEventEntity outboxEventEntity = createPendingEvent();

        outboxEventEntity.markAsFailed("first");
        outboxEventEntity.markAsFailed("second");

        outboxEventEntity.markAsPendingRetry();

        doThrow(new RuntimeException("third failure")).when(inventoryEventPublisher).publish(eq(KafkaTopics.INVENTORY_RESERVED), anyString(), anyString());

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        outboxEventProcessor.process();

        verify(outboxEventRepository).save(outboxEventEntity);

        assertThat(outboxEventEntity.getRetryCount()).isEqualTo(3);

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.DLT);
    }

    @Test
    @DisplayName("Should publish DLT event when max retries reached")
    void shouldPublishDltEventWhenMaxRetriesReached() {

        OutboxEventEntity outboxEventEntity = createPendingEvent();

        outboxEventEntity.markAsFailed("error1");
        outboxEventEntity.markAsFailed("error2");

        outboxEventEntity.markAsPendingRetry();

        doThrow(new RuntimeException("error3")).when(inventoryEventPublisher).publish(eq(KafkaTopics.INVENTORY_RESERVED), anyString(), anyString());

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        outboxEventProcessor.process();

        verify(inventoryEventPublisher).publish(eq(KafkaTopics.INVENTORY_RESERVATION_DLT), eq(outboxEventEntity.getAggregateId().toString()), anyString());
    }

    @Test
    @DisplayName("Should update entity before publishing")
    void shouldUpdateEntityBeforePublishing() {

        OutboxEventEntity outboxEventEntity = createPendingEvent();

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        outboxEventProcessor.process();

        verify(outboxEventRepository, atLeastOnce()).update(outboxEventEntity);

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should persist failure before retry")
    void shouldPersistFailureBeforeRetry() {

        OutboxEventEntity outboxEventEntity = createPendingEvent();

        doThrow(new RuntimeException("temporary failure")).when(inventoryEventPublisher).publish(anyString(), anyString(), anyString());

        when(outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        outboxEventProcessor.process();

        verify(outboxEventRepository, times(2)).update(outboxEventEntity);

        verify(outboxEventRepository).save(outboxEventEntity);

        assertThat(outboxEventEntity.getRetryCount()).isEqualTo(1);
    }

    private OutboxEventEntity createPendingEvent() {

        return new OutboxEventEntity(aggregateId, "ORDER", "INVENTORY_RESERVED", "{\"event\":\"payload\"}", OutboxStatus.PENDING);
    }
}