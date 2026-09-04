package com.lmf.fraud.fraudservice.infrastructure.kafka.config;

import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.contracts.FraudRejectedEvent;
import com.lmf.platform.messaging.OutboxTopicRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sem este bean o {@code OutboxRelay} do platform-messaging nunca é criado (é
 * {@code @ConditionalOnBean(OutboxTopicRouter.class)}) — os eventos ficariam presos em
 * {@code PENDING} na tabela {@code outbox_events} sem nenhum erro visível.
 */
@Configuration
public class OutboxRoutingConfig {

    @Bean
    public OutboxTopicRouter fraudOutboxTopicRouter() {

        return eventType -> switch (eventType) {
            case FraudApprovedEvent.TYPE -> KafkaTopics.FRAUD_APPROVED;
            case FraudRejectedEvent.TYPE -> KafkaTopics.FRAUD_REJECTED;
            default -> throw new IllegalStateException("No Kafka topic mapped for outbox eventType=" + eventType);
        };
    }
}
