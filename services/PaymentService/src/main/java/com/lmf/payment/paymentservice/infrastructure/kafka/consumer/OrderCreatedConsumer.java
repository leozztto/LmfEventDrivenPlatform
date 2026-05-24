package com.lmf.payment.paymentservice.infrastructure.kafka.consumer;

import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.events.OrderCreatedEvent;
import com.lmf.payment.paymentservice.infrastructure.kafka.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ProcessPaymentUseCase processPaymentUseCase;

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 2000, multiplier = 2.0), dltTopicSuffix = ".dlt")
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, containerFactory = "kafkaListenerContainerFactory")
    public void consume(OrderCreatedEvent orderCreatedEvent) {

        log.info("Received order created event. orderId={}", orderCreatedEvent.orderId());

        ProcessPaymentCommand processPaymentCommand = toPaymentCommand(orderCreatedEvent);

        processPaymentUseCase.execute(processPaymentCommand);
    }

    private ProcessPaymentCommand toPaymentCommand(OrderCreatedEvent orderCreatedEvent) {

        return new ProcessPaymentCommand(orderCreatedEvent.orderId(), orderCreatedEvent.customer().customerId(), orderCreatedEvent.totalAmount(), "BRL", orderCreatedEvent.payment().paymentMethod(), orderCreatedEvent.payment().installments());
    }
}
