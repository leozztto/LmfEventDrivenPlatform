package com.lmf.inventory.inventoryservice.infrastructure.kafka;

import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara os tópicos produzidos por este serviço (partições/replicação explícitas em vez de
 * auto-create). Desabilitável com {@code platform.kafka.declare-topics=false}.
 */
@Configuration
@ConditionalOnProperty(name = "platform.kafka.declare-topics", havingValue = "true", matchIfMissing = true)
public class KafkaTopicsConfig {

    @Bean
    public NewTopic inventoryReservedTopic() {
        return TopicBuilder.name(KafkaTopics.INVENTORY_RESERVED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic inventoryReservationFailedTopic() {
        return TopicBuilder.name(KafkaTopics.INVENTORY_RESERVATION_FAILED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.PRODUCT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic inventoryReservationDltTopic() {
        return TopicBuilder.name(KafkaTopics.INVENTORY_RESERVATION_DLT).partitions(3).replicas(1).build();
    }
}
