package com.lmf.payment.paymentservice.infrastructure.kafka.consumer;

import com.lmf.payment.paymentservice.application.service.InboxEventService;
import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.events.OrderCreatedEvent;
import com.lmf.payment.paymentservice.infrastructure.kafka.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ProcessPaymentUseCase processPaymentUseCase;

    private final InboxEventService inboxEventService;

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 2000, multiplier = 2.0), dltTopicSuffix = ".dlt")
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, containerFactory = "kafkaListenerContainerFactory")
    public void consume(OrderCreatedEvent orderCreatedEvent, @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Received order created event. orderId={}", orderCreatedEvent.orderId());

        if (inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())) {

            log.warn("Ignoring duplicated event. eventId={}", orderCreatedEvent.eventId());

            return;
        }

        inboxEventService.register(orderCreatedEvent.eventId().toString(), orderCreatedEvent.orderId(), "ORDER_CREATED");

        try {

            ProcessPaymentCommand processPaymentCommand = toPaymentCommand(orderCreatedEvent);

            processPaymentUseCase.execute(processPaymentCommand);

            inboxEventService.markProcessed(orderCreatedEvent.eventId().toString());

        } catch (Exception ex) {

            inboxEventService.markFailed(orderCreatedEvent.eventId().toString(), ex.getMessage());

            log.error("Error processing OrderCreatedEvent. eventId={}, error={}", orderCreatedEvent.eventId(), ex.getMessage(), ex);

            throw ex;
        }
    }

    private ProcessPaymentCommand toPaymentCommand(OrderCreatedEvent orderCreatedEvent) {

        return new ProcessPaymentCommand(orderCreatedEvent.orderId(), orderCreatedEvent.customer().customerId(), orderCreatedEvent.totalAmount(), "BRL", orderCreatedEvent.payment().paymentMethod(), orderCreatedEvent.payment().installments());
    }
}
