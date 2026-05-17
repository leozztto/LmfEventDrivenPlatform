package com.lmf.order.orderservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(
            String topic,
            String key,
            String payload
    ) {

        kafkaTemplate.send(
                topic,
                key,
                payload
        );
    }
}