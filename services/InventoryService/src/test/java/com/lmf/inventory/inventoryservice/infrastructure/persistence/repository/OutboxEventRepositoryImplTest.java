package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OutboxEventRepositoryImplTest {

    private SpringDataOutboxEventRepository springDataOutboxEventRepository;

    private OutboxEventRepositoryImpl outboxEventRepository;

    @BeforeEach
    void setUp() {

        springDataOutboxEventRepository = mock(SpringDataOutboxEventRepository.class);

        outboxEventRepository = new OutboxEventRepositoryImpl(springDataOutboxEventRepository);
    }

    @Test
    @DisplayName("Should save outbox event")
    void shouldSaveOutboxEvent() {

        OutboxEventEntity outboxEventEntity = mock(OutboxEventEntity.class);

        outboxEventRepository.save(outboxEventEntity);

        verify(springDataOutboxEventRepository).save(outboxEventEntity);
    }

    @Test
    @DisplayName("Should update outbox event")
    void shouldUpdateOutboxEvent() {

        OutboxEventEntity outboxEventEntity = mock(OutboxEventEntity.class);

        outboxEventRepository.update(outboxEventEntity);

        verify(springDataOutboxEventRepository).saveAndFlush(outboxEventEntity);
    }

    @Test
    @DisplayName("Should find top 100 pending events ordered by creation date")
    void shouldFindTop100PendingEventsOrderedByCreationDate() {

        List<OutboxEventEntity> outboxEventEntities = List.of(mock(OutboxEventEntity.class));

        when(springDataOutboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(outboxEventEntities);

        List<OutboxEventEntity> outboxEventEntityList = outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        assertThat(outboxEventEntityList).isEqualTo(outboxEventEntities);

        verify(springDataOutboxEventRepository).findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("Should return all outbox events")
    void shouldReturnAllOutboxEvents() {

        List<OutboxEventEntity> outboxEventEntities = List.of(mock(OutboxEventEntity.class), mock(OutboxEventEntity.class));

        when(springDataOutboxEventRepository.findAll()).thenReturn(outboxEventEntities);

        List<OutboxEventEntity> outboxEventEntityList = outboxEventRepository.findAll();

        assertThat(outboxEventEntityList).hasSize(2).isEqualTo(outboxEventEntities);

        verify(springDataOutboxEventRepository).findAll();
    }

    @Test
    @DisplayName("Should find outbox event by id")
    void shouldFindOutboxEventById() {

        UUID id = UUID.randomUUID();

        OutboxEventEntity outboxEventEntity = mock(OutboxEventEntity.class);

        when(springDataOutboxEventRepository.findById(id)).thenReturn(Optional.of(outboxEventEntity));

        Optional<OutboxEventEntity> result = outboxEventRepository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(outboxEventEntity);

        verify(springDataOutboxEventRepository).findById(id);
    }

    @Test
    @DisplayName("Should return empty when outbox event not found")
    void shouldReturnEmptyWhenOutboxEventNotFound() {

        UUID id = UUID.randomUUID();

        when(springDataOutboxEventRepository.findById(id)).thenReturn(Optional.empty());

        Optional<OutboxEventEntity> result = outboxEventRepository.findById(id);

        assertThat(result).isEmpty();

        verify(springDataOutboxEventRepository).findById(id);
    }
}