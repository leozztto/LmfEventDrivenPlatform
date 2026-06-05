package com.lmf.payment.paymentservice.unit.infrasctruture.persistence;

import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.payment.paymentservice.infrastructure.persistence.repository.OutboxEventRepositoryImpl;
import com.lmf.payment.paymentservice.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventRepositoryImplTest {

    @Mock
    private SpringDataOutboxEventRepository springDataOutboxEventRepository;

    @InjectMocks
    private OutboxEventRepositoryImpl outboxEventRepository;

    private OutboxEventEntity outboxEventEntity;

    @BeforeEach
    void setUp() {

        outboxEventEntity = new OutboxEventEntity(UUID.randomUUID(), "PAYMENT", "PAYMENT_CREATED", """
                {
                  "paymentId": "123",
                  "status": "APPROVED"
                }
                """, OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("Should save outbox event successfully")
    void shouldSaveOutboxEventSuccessfully() {

        outboxEventRepository.save(outboxEventEntity);

        verify(springDataOutboxEventRepository).save(outboxEventEntity);
    }

    @Test
    @DisplayName("Should find pending outbox events")
    void shouldFindPendingOutboxEvents() {

        when(springDataOutboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).thenReturn(List.of(outboxEventEntity));

        List<OutboxEventEntity> result = outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OutboxStatus.PENDING, result.get(0).getOutboxStatus());

        verify(springDataOutboxEventRepository).findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("Should update outbox event successfully")
    void shouldUpdateOutboxEventSuccessfully() {

        outboxEventRepository.update(outboxEventEntity);

        verify(springDataOutboxEventRepository).saveAndFlush(outboxEventEntity);
    }

    @Test
    @DisplayName("Should return all outbox events")
    void shouldReturnAllOutboxEvents() {

        when(springDataOutboxEventRepository.findAll()).thenReturn(List.of(outboxEventEntity));

        List<OutboxEventEntity> result = outboxEventRepository.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(springDataOutboxEventRepository).findAll();
    }

    @Test
    @DisplayName("Should find outbox event by id")
    void shouldFindOutboxEventById() {

        UUID id = UUID.randomUUID();

        when(springDataOutboxEventRepository.findById(id)).thenReturn(Optional.of(outboxEventEntity));

        Optional<OutboxEventEntity> result = outboxEventRepository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(outboxEventEntity, result.get());

        verify(springDataOutboxEventRepository).findById(id);
    }
}