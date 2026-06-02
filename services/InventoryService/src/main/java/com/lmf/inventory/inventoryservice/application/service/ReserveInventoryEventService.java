package com.lmf.inventory.inventoryservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.inventory.inventoryservice.domain.event.InventoryReservationFailedEvent;
import com.lmf.inventory.inventoryservice.domain.event.InventoryReservationSuccessEvent;
import com.lmf.inventory.inventoryservice.domain.exception.EventSerializationException;
import com.lmf.inventory.inventoryservice.domain.repository.OutboxEventRepository;
import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveInventoryEventService {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    public void publishSuccess(InventoryReservationSuccessEvent inventoryReservationSuccessEvent) {

        saveOutbox(inventoryReservationSuccessEvent.orderId(), "RESERVED_SUCCESS", inventoryReservationSuccessEvent, OutboxStatus.PENDING);
    }

    public void publishFailure(InventoryReservationFailedEvent inventoryReservationFailedEvent) {

        saveOutbox(inventoryReservationFailedEvent.orderId(), "RESERVED_FAILED", inventoryReservationFailedEvent, OutboxStatus.FAILED);
    }

    private void saveOutbox(UUID aggregateId, String eventType, Object payloadObject, OutboxStatus outboxStatus) {

        try {

            String payload = objectMapper.writeValueAsString(payloadObject);

            OutboxEventEntity outboxEventEntity = new OutboxEventEntity(aggregateId, "ORDER", eventType, payload, outboxStatus);

            outboxEventRepository.save(outboxEventEntity);

            log.info("Outbox event created. eventType={}, eventId={}", "PAYMENT_CREATED", outboxEventEntity.getId());

        } catch (JsonProcessingException ex) {

            log.error("Failed to serialize payment created event. payment={}", payloadObject, ex);

            throw new EventSerializationException("Failed to serialize event", ex);
        }
    }
}
