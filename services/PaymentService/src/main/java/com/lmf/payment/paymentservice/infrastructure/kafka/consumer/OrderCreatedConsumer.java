package com.lmf.payment.paymentservice.infrastructure.kafka.consumer;

import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ProcessPaymentUseCase processPaymentUseCase;

    @KafkaListener(
            topics = "order.created",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(OrderCreatedEvent orderCreatedEvent) {

        log.info("Received order created event. orderId={}", orderCreatedEvent.orderId());

        ProcessPaymentCommand command = new ProcessPaymentCommand(orderCreatedEvent.orderId(), orderCreatedEvent.totalAmount(), orderCreatedEvent.payment().paymentMethod(), orderCreatedEvent.payment().installments());

        processPaymentUseCase.execute(command);
    }
}
