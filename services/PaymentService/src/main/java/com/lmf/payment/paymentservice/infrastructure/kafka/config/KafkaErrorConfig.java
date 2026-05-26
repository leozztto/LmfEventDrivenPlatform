package com.lmf.payment.paymentservice.infrastructure.kafka.config;

import com.lmf.payment.paymentservice.domain.exception.BusinessException;
import com.lmf.payment.paymentservice.infrastructure.exception.NonRetryableException;
import com.lmf.payment.paymentservice.infrastructure.exception.RetryableException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

@Configuration
public class KafkaErrorConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) -> new TopicPartition(record.topic() + ".dlt", record.partition()));

        FixedBackOff backOff = new FixedBackOff(3000L, 3L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addRetryableExceptions(RetryableException.class, SQLException.class, TimeoutException.class);

        errorHandler.addNotRetryableExceptions(BusinessException.class, NonRetryableException.class, IllegalArgumentException.class);

        return errorHandler;
    }
}
