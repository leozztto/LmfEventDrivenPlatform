package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.InboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InboxEventRepositoryImplTest {

    private SpringDataInboxEventRepository springDataInboxEventRepository;

    private InboxEventRepositoryImpl repository;

    @BeforeEach
    void setUp() {

        springDataInboxEventRepository = mock(SpringDataInboxEventRepository.class);

        repository = new InboxEventRepositoryImpl(springDataInboxEventRepository);
    }

    @Test
    @DisplayName("Should delegate existsByEventId")
    void shouldDelegateExistsByEventId() {

        String eventId = "event-123";

        when(springDataInboxEventRepository.existsByEventId(eventId)).thenReturn(true);

        boolean result = repository.existsByEventId(eventId);

        assertThat(result).isTrue();

        verify(springDataInboxEventRepository).existsByEventId(eventId);
    }

    @Test
    @DisplayName("Should save inbox event")
    void shouldSaveInboxEvent() {

        InboxEventEntity inboxEventEntity = mock(InboxEventEntity.class);

        when(springDataInboxEventRepository.save(inboxEventEntity)).thenReturn(inboxEventEntity);

        InboxEventEntity savedInboxEventEntity = repository.save(inboxEventEntity);

        assertThat(savedInboxEventEntity).isSameAs(inboxEventEntity);

        verify(springDataInboxEventRepository).save(inboxEventEntity);
    }

    @Test
    @DisplayName("Should find inbox event by event id")
    void shouldFindInboxEventByEventId() {

        String eventId = "event-123";

        InboxEventEntity inboxEventEntity = mock(InboxEventEntity.class);

        when(springDataInboxEventRepository.findByEventId(eventId)).thenReturn(Optional.of(inboxEventEntity));

        Optional<InboxEventEntity> savedInboxEventEntity = repository.findByEventId(eventId);

        assertThat(savedInboxEventEntity).isPresent();
        assertThat(savedInboxEventEntity.get()).isSameAs(inboxEventEntity);

        verify(springDataInboxEventRepository).findByEventId(eventId);
    }

    @Test
    @DisplayName("Should return empty when inbox event not found")
    void shouldReturnEmptyWhenInboxEventNotFound() {

        String eventId = "event-123";

        when(springDataInboxEventRepository.findByEventId(eventId)).thenReturn(Optional.empty());

        Optional<InboxEventEntity> inboxEventEntity = repository.findByEventId(eventId);

        assertThat(inboxEventEntity).isEmpty();

        verify(springDataInboxEventRepository).findByEventId(eventId);
    }

    @Test
    @DisplayName("Should return false when event does not exist")
    void shouldReturnFalseWhenEventDoesNotExist() {

        String eventId = "event-123";

        when(springDataInboxEventRepository.existsByEventId(eventId)).thenReturn(false);

        boolean result = repository.existsByEventId(eventId);

        assertThat(result).isFalse();

        verify(springDataInboxEventRepository).existsByEventId(eventId);
    }
}