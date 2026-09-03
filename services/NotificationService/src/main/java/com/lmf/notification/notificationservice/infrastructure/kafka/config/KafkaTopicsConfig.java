package com.lmf.notification.notificationservice.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara as DLTs de consumo deste serviço (uma por tópico consumido), em vez de depender do
 * auto-create do broker. Desabilitável com {@code platform.kafka.declare-topics=false}.
 */
@Configuration
@ConditionalOnProperty(name = "platform.kafka.declare-topics", havingValue = "true", matchIfMissing = true)
public class KafkaTopicsConfig {

    @Bean
    public NewTopic orderCreatedDltTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CREATED + ".dlt").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentApprovedDltTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_APPROVED + ".dlt").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedDltTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_FAILED + ".dlt").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic inventoryReservationFailedDltTopic() {
        return TopicBuilder.name(KafkaTopics.INVENTORY_RESERVATION_FAILED + ".dlt").partitions(3).replicas(1).build();
    }
}
