package com.lmf.payment.paymentservice.infrastructure.kafka.consumer;

import com.lmf.payment.paymentservice.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreatedDltConsumer {

    @KafkaListener(topics = "order.created.DLT", groupId = "payment-service-dlt-group")
    public void consume(OrderCreatedEvent orderCreatedEvent) {

        log.error("Message sent to DLT. orderId={}", orderCreatedEvent.orderId());

        log.error("Manual intervention required for orderId={}", orderCreatedEvent.orderId());
    }
}