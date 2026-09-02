package com.lmf.inventory.inventoryservice.infrastructure.kafka;

import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import com.lmf.platform.messaging.OutboxTopicRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutboxRoutingConfig {

    @Bean
    public OutboxTopicRouter inventoryOutboxTopicRouter() {

        return eventType -> switch (eventType) {
            case "INVENTORY_RESERVED" -> KafkaTopics.INVENTORY_RESERVED;
            case "INVENTORY_RESERVATION_FAILED" -> KafkaTopics.INVENTORY_RESERVATION_FAILED;
            case "PRODUCT_CREATED" -> KafkaTopics.PRODUCT_CREATED;
            default -> throw new IllegalStateException("No Kafka topic mapped for outbox eventType=" + eventType);
        };
    }
}
