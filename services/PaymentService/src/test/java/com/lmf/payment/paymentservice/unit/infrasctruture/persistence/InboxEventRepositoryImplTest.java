package com.lmf.payment.paymentservice.unit.infrasctruture.persistence;

import com.lmf.payment.paymentservice.infrastructure.inbox.InboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.InboxEventEntity;
import com.lmf.payment.paymentservice.infrastructure.persistence.repository.InboxEventRepositoryImpl;
import com.lmf.payment.paymentservice.infrastructure.persistence.repository.SpringDataInboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboxEventRepositoryImplTest {

    @Mock
    private SpringDataInboxEventRepository springDataInboxEventRepository;

    @InjectMocks
    private InboxEventRepositoryImpl inboxEventRepository;

    private InboxEventEntity inboxEventEntity;

    @BeforeEach
    void setUp() {

        inboxEventEntity = new InboxEventEntity("event-123", UUID.randomUUID(), "ORDER_CREATED");
    }

    @Test
    @DisplayName("Should return true when event already exists")
    void shouldReturnTrueWhenEventAlreadyExists() {

        when(springDataInboxEventRepository.existsByEventId("event-123")).thenReturn(true);

        boolean exists = inboxEventRepository.existsByEventId("event-123");

        assertTrue(exists);

        verify(springDataInboxEventRepository).existsByEventId("event-123");
    }

    @Test
    @DisplayName("Should return false when event does not exist")
    void shouldReturnFalseWhenEventDoesNotExist() {

        when(springDataInboxEventRepository.existsByEventId("event-123")).thenReturn(false);

        boolean exists = inboxEventRepository.existsByEventId("event-123");

        assertFalse(exists);

        verify(springDataInboxEventRepository).existsByEventId("event-123");
    }

    @Test
    @DisplayName("Should save inbox event successfully")
    void shouldSaveInboxEventSuccessfully() {

        when(springDataInboxEventRepository.save(inboxEventEntity)).thenReturn(inboxEventEntity);

        InboxEventEntity savedEntity = inboxEventRepository.save(inboxEventEntity);

        assertNotNull(savedEntity);
        assertEquals("event-123", savedEntity.getEventId());
        assertEquals(InboxStatus.RECEIVED, savedEntity.getInboxStatus());

        verify(springDataInboxEventRepository).save(inboxEventEntity);
    }

    @Test
    @DisplayName("Should find inbox event by event id")
    void shouldFindInboxEventByEventId() {

        when(springDataInboxEventRepository.findByEventId("event-123")).thenReturn(Optional.of(inboxEventEntity));

        Optional<InboxEventEntity> result = inboxEventRepository.findByEventId("event-123");

        assertTrue(result.isPresent());

        assertEquals("event-123", result.get().getEventId());

        verify(springDataInboxEventRepository).findByEventId("event-123");
    }

    @Test
    @DisplayName("Should return empty optional when event not found")
    void shouldReturnEmptyOptionalWhenEventNotFound() {

        when(springDataInboxEventRepository.findByEventId("event-123")).thenReturn(Optional.empty());

        Optional<InboxEventEntity> result = inboxEventRepository.findByEventId("event-123");

        assertTrue(result.isEmpty());

        verify(springDataInboxEventRepository).findByEventId("event-123");
    }
}