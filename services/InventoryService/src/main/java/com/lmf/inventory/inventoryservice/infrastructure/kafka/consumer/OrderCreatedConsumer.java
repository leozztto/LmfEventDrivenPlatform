package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.service.InboxEventService;
import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.inventory.inventoryservice.domain.event.OrderCreatedEvent;
import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ReserveInventoryUseCase reserveInventoryUseCase;

    private final InboxEventService inboxEventService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-service-group")
    public void consume(OrderCreatedEvent orderCreatedEvent) {

        if (inboxEventService.isDuplicate(orderCreatedEvent.eventId().toString())) {

            return;
        }

        reserveInventoryUseCase.execute(orderCreatedEvent);

        inboxEventService.register(orderCreatedEvent.eventId().toString(), orderCreatedEvent.orderId(), "RESERVED_CREATED");
    }
}
