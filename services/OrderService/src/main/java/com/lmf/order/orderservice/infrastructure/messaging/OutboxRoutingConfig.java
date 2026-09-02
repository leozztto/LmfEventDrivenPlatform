package com.lmf.order.orderservice.infrastructure.messaging;

import com.lmf.platform.messaging.OutboxTopicRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutboxRoutingConfig {

    @Bean
    public OutboxTopicRouter orderOutboxTopicRouter() {

        return eventType -> switch (eventType) {
            case "ORDER_CREATED" -> KafkaTopics.ORDER_CREATED;
            default -> throw new IllegalStateException("No Kafka topic mapped for outbox eventType=" + eventType);
        };
    }
}
