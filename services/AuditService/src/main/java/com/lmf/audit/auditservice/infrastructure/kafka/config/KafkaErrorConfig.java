package com.lmf.audit.auditservice.infrastructure.kafka.config;

import com.lmf.platform.messaging.DuplicateEventException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Tratamento de erro dos consumidores: retentativa com backoff exponencial e, esgotadas as
 * tentativas, envio da mensagem para {@code <topic>.dlt} (nunca retentativa infinita).
 */
@Slf4j
@Configuration
public class KafkaErrorConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (record, exception) -> {

            log.error("Sending message to DLT. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), exception.getMessage());

            return new TopicPartition(record.topic() + ".dlt", record.partition());
        });

        ExponentialBackOff backOff = new ExponentialBackOff();

        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10000L);
        backOff.setMaxElapsedTime(60000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Uma corrida de inbox entre consumidores não deve ser retentada — o outro já está processando.
        errorHandler.addNotRetryableExceptions(DuplicateEventException.class);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("Retrying kafka message. topic={}, partition={}, offset={}, attempt={}, error={}",
                        record.topic(), record.partition(), record.offset(), deliveryAttempt, ex.getMessage()));

        return errorHandler;
    }
}
