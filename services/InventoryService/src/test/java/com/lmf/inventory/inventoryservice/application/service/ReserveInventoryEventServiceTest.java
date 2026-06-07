package com.lmf.inventory.inventoryservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.inventory.inventoryservice.domain.event.InventoryReservationFailedEvent;
import com.lmf.inventory.inventoryservice.domain.event.InventoryReservationSuccessEvent;
import com.lmf.inventory.inventoryservice.domain.exception.EventSerializationException;
import com.lmf.inventory.inventoryservice.domain.repository.OutboxEventRepository;
import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReserveInventoryEventServiceTest {

    private OutboxEventRepository outboxEventRepository;

    private ObjectMapper objectMapper;

    private ReserveInventoryEventService reserveInventoryEventService;

    @BeforeEach
    void setUp() {

        outboxEventRepository = mock(OutboxEventRepository.class);

        objectMapper = mock(ObjectMapper.class);

        reserveInventoryEventService = new ReserveInventoryEventService(outboxEventRepository, objectMapper);
    }

    @Test
    @DisplayName("Should publish reservation success event")
    void shouldPublishReservationSuccessEvent() throws Exception {

        InventoryReservationSuccessEvent inventoryReservationSuccessEvent = createSuccessEvent();

        when(objectMapper.writeValueAsString(inventoryReservationSuccessEvent)).thenReturn("{\"status\":\"SUCCESS\"}");

        reserveInventoryEventService.publishSuccess(inventoryReservationSuccessEvent);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(outboxEventRepository).save(captor.capture());

        OutboxEventEntity outboxEventEntity = captor.getValue();

        assertThat(outboxEventEntity.getAggregateId()).isEqualTo(inventoryReservationSuccessEvent.orderId());

        assertThat(outboxEventEntity.getAggregateType()).isEqualTo("ORDER");

        assertThat(outboxEventEntity.getEventType()).isEqualTo("RESERVED_SUCCESS");

        assertThat(outboxEventEntity.getPayload()).isEqualTo("{\"status\":\"SUCCESS\"}");

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("Should publish reservation failed event")
    void shouldPublishReservationFailedEvent() throws Exception {

        InventoryReservationFailedEvent inventoryReservationFailedEvent = createFailedEvent();

        when(objectMapper.writeValueAsString(inventoryReservationFailedEvent)).thenReturn("{\"status\":\"FAILED\"}");

        reserveInventoryEventService.publishFailure(inventoryReservationFailedEvent);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(outboxEventRepository).save(captor.capture());

        OutboxEventEntity outboxEventEntity = captor.getValue();

        assertThat(outboxEventEntity.getAggregateId()).isEqualTo(inventoryReservationFailedEvent.orderId());

        assertThat(outboxEventEntity.getAggregateType()).isEqualTo("ORDER");

        assertThat(outboxEventEntity.getEventType()).isEqualTo("RESERVED_FAILED");

        assertThat(outboxEventEntity.getPayload()).isEqualTo("{\"status\":\"FAILED\"}");

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
    }

    @Test
    @DisplayName("Should serialize success event")
    void shouldSerializeSuccessEvent() throws Exception {

        InventoryReservationSuccessEvent inventoryReservationSuccessEvent = createSuccessEvent();

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        reserveInventoryEventService.publishSuccess(inventoryReservationSuccessEvent);

        verify(objectMapper).writeValueAsString(inventoryReservationSuccessEvent);
    }

    @Test
    @DisplayName("Should serialize failure event")
    void shouldSerializeFailureEvent() throws Exception {

        InventoryReservationFailedEvent inventoryReservationFailedEvent = createFailedEvent();

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        reserveInventoryEventService.publishFailure(inventoryReservationFailedEvent);

        verify(objectMapper).writeValueAsString(inventoryReservationFailedEvent);
    }

    @Test
    @DisplayName("Should throw EventSerializationException for success event")
    void shouldThrowEventSerializationExceptionForSuccessEvent() throws Exception {

        InventoryReservationSuccessEvent inventoryReservationSuccessEvent = createSuccessEvent();

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("serialization error") {
        });

        assertThatThrownBy(() -> reserveInventoryEventService.publishSuccess(inventoryReservationSuccessEvent)).isInstanceOf(EventSerializationException.class).hasMessageContaining("Failed to serialize event");

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EventSerializationException for failed event")
    void shouldThrowEventSerializationExceptionForFailedEvent() throws Exception {

        InventoryReservationFailedEvent inventoryReservationFailedEvent = createFailedEvent();

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("serialization error") {
        });

        assertThatThrownBy(() -> reserveInventoryEventService.publishFailure(inventoryReservationFailedEvent)).isInstanceOf(EventSerializationException.class).hasMessageContaining("Failed to serialize event");

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should preserve original jackson exception")
    void shouldPreserveOriginalJacksonException() throws Exception {

        InventoryReservationSuccessEvent inventoryReservationSuccessEvent = createSuccessEvent();

        JsonProcessingException exception = new JsonProcessingException("jackson failure") {
        };

        when(objectMapper.writeValueAsString(any())).thenThrow(exception);

        assertThatThrownBy(() -> reserveInventoryEventService.publishSuccess(inventoryReservationSuccessEvent)).isInstanceOf(EventSerializationException.class).hasCause(exception);
    }

    private InventoryReservationSuccessEvent createSuccessEvent() {

        return new InventoryReservationSuccessEvent(UUID.randomUUID(), "INVENTORY_RESERVED", "v1", OffsetDateTime.now(), UUID.randomUUID(), UUID.randomUUID());
    }

    private InventoryReservationFailedEvent createFailedEvent() {

        return new InventoryReservationFailedEvent(UUID.randomUUID(), "INVENTORY_RESERVATION_FAILED", "v1", OffsetDateTime.now(), UUID.randomUUID(), UUID.randomUUID(), "Insufficient stock");
    }
}