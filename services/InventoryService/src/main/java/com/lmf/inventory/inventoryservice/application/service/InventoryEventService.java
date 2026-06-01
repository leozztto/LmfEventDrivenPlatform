package com.lmf.inventory.inventoryservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.inventory.inventoryservice.domain.event.ProductCreatedEvent;
import com.lmf.inventory.inventoryservice.domain.exception.EventSerializationException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
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
public class InventoryEventService {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    public void publish(Product product) {

        ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.of(product);

        saveOutbox(productCreatedEvent.productId(), "PRODUCT_CREATED", productCreatedEvent);
    }

    private void saveOutbox(UUID aggregateId, String eventType, Object payloadObject) {

        try {

            String payload = objectMapper.writeValueAsString(payloadObject);

            OutboxEventEntity outboxEventEntity = new OutboxEventEntity(aggregateId, "PRODUCT", eventType, payload, OutboxStatus.PUBLISHED);

            outboxEventRepository.save(outboxEventEntity);

            log.info("Outbox event created. eventType={}, eventId={}", "PRODUCT_CREATED", outboxEventEntity.getId());

        } catch (JsonProcessingException ex) {

            log.error("Failed to serialize product created event. product={}", payloadObject, ex);

            throw new EventSerializationException("Failed to serialize event", ex);
        }
    }

}
