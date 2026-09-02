package com.lmf.payment.paymentservice.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "platform.kafka.declare-topics", havingValue = "true", matchIfMissing = true)
public class KafkaTopicsConfig {

    @Bean
    public NewTopic paymentApprovedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_APPROVED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_FAILED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentProcessingTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_PROCESSING).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentOutboxDltTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_OUTBOX_DLT).partitions(3).replicas(1).build();
    }
}
