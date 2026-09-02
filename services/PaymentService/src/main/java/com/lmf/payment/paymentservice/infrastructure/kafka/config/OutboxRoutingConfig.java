package com.lmf.payment.paymentservice.infrastructure.kafka.config;

import com.lmf.platform.messaging.OutboxTopicRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutboxRoutingConfig {

    @Bean
    public OutboxTopicRouter paymentOutboxTopicRouter() {

        return eventType -> switch (eventType) {
            case "PAYMENT_APPROVED" -> KafkaTopics.PAYMENT_APPROVED;
            case "PAYMENT_FAILED" -> KafkaTopics.PAYMENT_FAILED;
            case "PAYMENT_PROCESSING" -> KafkaTopics.PAYMENT_PROCESSING;
            default -> throw new IllegalStateException("No Kafka topic mapped for outbox eventType=" + eventType);
        };
    }
}
