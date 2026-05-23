package com.lmf.payment.paymentservice.infrastructure.kafka.producer;

import com.lmf.payment.paymentservice.events.PaymentCreatedEvent;
import com.lmf.payment.paymentservice.ports.output.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.lmf.payment.paymentservice.infrastructure.kafka.config.KafkaTopics.PAYMENT_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaProducer implements PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(PaymentCreatedEvent paymentCreatedEvent) {

        kafkaTemplate.send(PAYMENT_CREATED, paymentCreatedEvent.orderId().toString(), paymentCreatedEvent);

        log.info("PaymentCreatedEvent published. paymentId={}, orderId={}", paymentCreatedEvent.paymentId(), paymentCreatedEvent.orderId());
    }
}
