package com.lmf.inventory.inventoryservice.infrastructure.kafka.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

class InventoryEventPublisherTest {

    private KafkaTemplate<String, String> kafkaTemplate;

    private InventoryEventPublisher inventoryEventPublisher;

    @BeforeEach
    void setup() {

        kafkaTemplate = mock(KafkaTemplate.class);

        inventoryEventPublisher = new InventoryEventPublisher(kafkaTemplate);
    }

    @Test
    @DisplayName("Should publish event to kafka")
    void shouldPublishEventToKafka() {

        String topic = "inventory-events";
        String key = "product-123";
        String payload = """
                {
                    "eventType":"PRODUCT_CREATED"
                }
                """;

        inventoryEventPublisher.publish(topic, key, payload);

        verify(kafkaTemplate).send(topic, key, payload);
    }

    @Test
    @DisplayName("Should publish event only once")
    void shouldPublishEventOnlyOnce() {

        inventoryEventPublisher.publish("topic", "key", "payload");

        verify(kafkaTemplate, times(1)).send("topic", "key", "payload");

        verifyNoMoreInteractions(kafkaTemplate);
    }
}