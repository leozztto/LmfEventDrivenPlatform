package com.lmf.payment.paymentservice.infrastructure.kafka.config;

import com.lmf.payment.paymentservice.domain.exception.PaymentDeclinedException;
import com.lmf.payment.paymentservice.domain.exception.PaymentGatewayException;
import com.lmf.payment.paymentservice.domain.exception.PaymentTimeoutException;
import com.lmf.payment.paymentservice.infrastructure.exception.NonRetryableException;
import com.lmf.payment.paymentservice.infrastructure.exception.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Configuration
public class KafkaErrorConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (record, exception) -> {

            log.error("Sending message to DLT. topic={}, partition={}, offset={}, error={}", record.topic(), record.partition(), record.offset(), exception.getMessage());

            return new TopicPartition(record.topic() + ".dlt", record.partition());
        });

        ExponentialBackOff backOff = new ExponentialBackOff();

        backOff.setInitialInterval(2000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(30000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addRetryableExceptions(RetryableException.class, PaymentGatewayException.class, PaymentTimeoutException.class, SQLException.class, TimeoutException.class);

        errorHandler.addNotRetryableExceptions(NonRetryableException.class, PaymentDeclinedException.class, IllegalArgumentException.class);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {

            log.warn("Retrying kafka message. topic={}, partition={}, offset={}, attempt={}, error={}", record.topic(), record.partition(), record.offset(), deliveryAttempt, ex.getMessage());
        });

        return errorHandler;
    }
}