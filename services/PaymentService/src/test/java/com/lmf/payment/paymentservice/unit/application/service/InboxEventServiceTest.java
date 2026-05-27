package com.lmf.payment.paymentservice.unit.application.service;

import com.lmf.payment.paymentservice.application.service.InboxEventService;
import com.lmf.payment.paymentservice.domain.repository.InboxEventRepository;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.InboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
    @DisplayName("Deve retornar true quando evento for duplicado")
    void shouldReturnTrueWhenEventIsDuplicate() {

        String eventId = "event-123";

        when(inboxEventRepository.existsByEventId(eventId)).thenReturn(true);

        boolean duplicated = inboxEventService.isDuplicate(eventId);

        assertTrue(duplicated);

        verify(inboxEventRepository).existsByEventId(eventId);
    }

    @Test
    @DisplayName("Deve retornar false quando evento nao for duplicado")
    void shouldReturnFalseWhenEventIsNotDuplicate() {

        String eventId = "event-123";

        when(inboxEventRepository.existsByEventId(eventId)).thenReturn(false);

        boolean duplicated = inboxEventService.isDuplicate(eventId);

        assertFalse(duplicated);

        verify(inboxEventRepository).existsByEventId(eventId);
    }

    @Test
    @DisplayName("Deve registrar inbox event com sucesso")
    void shouldRegisterInboxEventSuccessfully() {

        String eventId = "event-123";
        UUID aggregateId = UUID.randomUUID();
        String eventType = "PAYMENT_CREATED";

        ArgumentCaptor<InboxEventEntity> captor = ArgumentCaptor.forClass(InboxEventEntity.class);

        InboxEventEntity result = inboxEventService.register(eventId, aggregateId, eventType);

        verify(inboxEventRepository).save(captor.capture());

        InboxEventEntity savedEntity = captor.getValue();

        assertNotNull(result);
        assertEquals(eventId, savedEntity.getEventId());
        assertEquals(aggregateId, savedEntity.getAggregateId());
        assertEquals(eventType, savedEntity.getEventType());
    }

    @Test
    @DisplayName("Deve marcar evento como processado")
    void shouldMarkEventAsProcessed() {

        String eventId = "event-123";

        InboxEventEntity inboxEventEntity = mock(InboxEventEntity.class);

        when(inboxEventRepository.findByEventId(eventId)).thenReturn(Optional.of(inboxEventEntity));

        inboxEventService.markProcessed(eventId);

        verify(inboxEventRepository).findByEventId(eventId);
        verify(inboxEventEntity).markProcessed();
        verify(inboxEventRepository).save(inboxEventEntity);
    }

    @Test
    @DisplayName("Nao deve salvar quando evento processado nao existir")
    void shouldNotSaveWhenProcessedEventDoesNotExist() {

        String eventId = "event-123";

        when(inboxEventRepository.findByEventId(eventId)).thenReturn(Optional.empty());

        inboxEventService.markProcessed(eventId);

        verify(inboxEventRepository).findByEventId(eventId);

        verify(inboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve marcar evento como falho")
    void shouldMarkEventAsFailed() {

        String eventId = "event-123";
        String reason = "Gateway timeout";

        InboxEventEntity inboxEventEntity = mock(InboxEventEntity.class);

        when(inboxEventRepository.findByEventId(eventId)).thenReturn(Optional.of(inboxEventEntity));

        inboxEventService.markFailed(eventId, reason);

        verify(inboxEventRepository).findByEventId(eventId);
        verify(inboxEventEntity).markFailed(reason);
        verify(inboxEventRepository).save(inboxEventEntity);
    }

    @Test
    @DisplayName("Nao deve salvar quando evento falho nao existir")
    void shouldNotSaveWhenFailedEventDoesNotExist() {

        String eventId = "event-123";

        when(inboxEventRepository.findByEventId(eventId)).thenReturn(Optional.empty());

        inboxEventService.markFailed(eventId, "error");

        verify(inboxEventRepository).findByEventId(eventId);

        verify(inboxEventRepository, never()).save(any());
    }
}
