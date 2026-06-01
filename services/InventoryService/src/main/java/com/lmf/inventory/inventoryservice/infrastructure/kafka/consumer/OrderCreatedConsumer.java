package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.service.InboxEventService;
import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.inventory.inventoryservice.domain.event.OrderCreatedEvent;
import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ReserveInventoryUseCase reserveInventoryUseCase;

    private final InboxEventService inboxEventService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-service-group")
    public void consume(OrderCreatedEvent orderCreatedEvent) {

        String eventId = orderCreatedEvent.eventId().toString();

        if (inboxEventService.isDuplicate(eventId)) {

            return;
        }


        try {

            inboxEventService.register(eventId, orderCreatedEvent.orderId(), orderCreatedEvent.eventType());

            reserveInventoryUseCase.execute(orderCreatedEvent);

            inboxEventService.markProcessed(eventId);

        } catch (Exception exception) {

            inboxEventService.markFailed(eventId, extractReason(exception));

            log.error("Error processing OrderCreatedEvent. eventId={}", eventId, exception);

            throw exception;
        }
    }

    private String extractReason(Throwable throwable) {

        return Optional.ofNullable(throwable.getMessage()).orElse(throwable.getClass().getSimpleName());
    }
}
