package com.lmf.fraud.fraudservice.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara os tópicos que este serviço produz ({@code fraud.approved}, {@code fraud.rejected}) e a
 * DLT do único tópico que consome ({@code order.created.dlt} — também declarada pelo OrderService e
 * pelo NotificationService; redeclarar o mesmo {@code NewTopic} em múltiplos serviços é seguro e já
 * é o padrão do projeto). Desabilitável com {@code platform.kafka.declare-topics=false}.
 */
@Configuration
@ConditionalOnProperty(name = "platform.kafka.declare-topics", havingValue = "true", matchIfMissing = true)
public class KafkaTopicsConfig {

    @Bean
    public NewTopic fraudApprovedTopic() {
        return TopicBuilder.name(KafkaTopics.FRAUD_APPROVED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic fraudRejectedTopic() {
        return TopicBuilder.name(KafkaTopics.FRAUD_REJECTED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic orderCreatedDltTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CREATED + ".dlt").partitions(3).replicas(1).build();
    }
}
