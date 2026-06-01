package com.lmf.inventory.inventoryservice.infrastructure.kafka.producer;

import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(OutboxEventEntity outboxEventEntity) {

        switch (outboxEventEntity.getEventType()) {

            case "InventoryReservedEvent" ->
                    kafkaTemplate.send(KafkaTopics.INVENTORY_RESERVED, outboxEventEntity.getAggregateId().toString(), outboxEventEntity.getPayload());

            case "InventoryReservationFailedEvent" ->
                    kafkaTemplate.send(KafkaTopics.INVENTORY_RESERVATION_FAILED, outboxEventEntity.getAggregateId().toString(), outboxEventEntity.getPayload());

            default -> throw new IllegalArgumentException("Unsupported event: " + outboxEventEntity.getEventType());
        }
    }
}
