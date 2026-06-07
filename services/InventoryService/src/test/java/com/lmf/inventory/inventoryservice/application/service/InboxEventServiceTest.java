package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.domain.exception.DuplicateEventException;
import com.lmf.inventory.inventoryservice.domain.repository.InboxEventRepository;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.InboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class InboxEventServiceTest {

    private InboxEventRepository inboxEventRepository;

    private InboxEventService inboxEventService;

    @BeforeEach
    void setUp() {

        inboxEventRepository = mock(InboxEventRepository.class);

        inboxEventService = new InboxEventService(inboxEventRepository);
    }

    @Test
    @DisplayName("Should return false when event is not duplicated")
    void shouldReturnFalseWhenEventIsNotDuplicated() {

        String eventId = UUID.randomUUID().toString();

        when(inboxEventRepository.existsByEventId(eventId)).thenReturn(false);

        boolean result = inboxEventService.isDuplicate(eventId);

        assertThat(result).isFalse();

        verify(inboxEventRepository).existsByEventId(eventId);
    }

    @Test
    @DisplayName("Should return true when event is duplicated")
    void shouldReturnTrueWhenEventIsDuplicated() {

        String eventId = UUID.randomUUID().toString();

        when(inboxEventRepository.existsByEventId(eventId)).thenReturn(true);

        boolean result = inboxEventService.isDuplicate(eventId);

        assertThat(result).isTrue();

        verify(inboxEventRepository).existsByEventId(eventId);
    }

    @Test
    @DisplayName("Should register inbox event successfully")
    void shouldRegisterInboxEventSuccessfully() {

        String eventId = UUID.randomUUID().toString();
        UUID aggregateId = UUID.randomUUID();
        String eventType = "ORDER_CREATED";

        InboxEventEntity savedEntity = mock(InboxEventEntity.class);

        when(inboxEventRepository.save(any(InboxEventEntity.class))).thenReturn(savedEntity);

        InboxEventEntity inboxEventEntity = inboxEventService.register(eventId, aggregateId, eventType);

        assertThat(inboxEventEntity).isSameAs(savedEntity);

        verify(inboxEventRepository).save(any(InboxEventEntity.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEventException when event already exists")
    void shouldThrowDuplicateEventException() {

        String eventId = UUID.randomUUID().toString();

        when(inboxEventRepository.save(any(InboxEventEntity.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> inboxEventService.register(eventId, UUID.randomUUID(), "ORDER_CREATED")).isInstanceOf(DuplicateEventException.class);

        verify(inboxEventRepository).save(any(InboxEventEntity.class));
    }

    @Test
    @DisplayName("Should mark event as processed")
    void shouldMarkEventAsProcessed() {

        String eventId = UUID.randomUUID().toString();

        InboxEventEntity inboxEventEntity = mock(InboxEventEntity.class);

        when(inboxEventRepository.findByEventId(eventId)).thenReturn(Optional.of(inboxEventEntity));

        inboxEventService.markProcessed(eventId);

        verify(inboxEventRepository).findByEventId(eventId);

        verify(inboxEventEntity).markProcessed();

        verify(inboxEventRepository).save(inboxEventEntity);
    }

    @Test
    @DisplayName("Should do nothing when event not found while marking processed")
    void shouldDoNothingWhenEventNotFoundWhileMarkingProcessed() {

        String eventId = UUID.randomUUID().toString();

        when(inboxEventRepository.findByEventId(eventId)).thenReturn(Optional.empty());

        inboxEventService.markProcessed(eventId);

        verify(inboxEventRepository).findByEventId(eventId);

        verify(inboxEventRepository, never()).save(any());

        verifyNoMoreInteractions(inboxEventRepository);
    }

    @Test
    @DisplayName("Should mark event as failed")
    void shouldMarkEventAsFailed() {

        String eventId = UUID.randomUUID().toString();

        String reason = "Inventory unavailable";

        InboxEventEntity inboxEventEntity = mock(InboxEventEntity.class);

        when(inboxEventRepository.findByEventId(eventId)).thenReturn(Optional.of(inboxEventEntity));

        inboxEventService.markFailed(eventId, reason);

        verify(inboxEventRepository).findByEventId(eventId);

        verify(inboxEventEntity).markFailed(reason);

        verify(inboxEventRepository).save(inboxEventEntity);
    }

    @Test
    @DisplayName("Should do nothing when event not found while marking failed")
    void shouldDoNothingWhenEventNotFoundWhileMarkingFailed() {

        String eventId = UUID.randomUUID().toString();

        when(inboxEventRepository.findByEventId(eventId)).thenReturn(Optional.empty());

        inboxEventService.markFailed(eventId, "failure reason");

        verify(inboxEventRepository).findByEventId(eventId);

        verify(inboxEventRepository, never()).save(any());

        verifyNoMoreInteractions(inboxEventRepository);
    }
}