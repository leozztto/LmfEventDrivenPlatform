package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.service.InboxEventService;
import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.inventory.inventoryservice.domain.event.OrderCreatedEvent;
import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer extends AbstractInboxConsumer<OrderCreatedEvent> {

    private final ReserveInventoryUseCase reserveInventoryUseCase;

    public OrderCreatedConsumer(InboxEventService inboxEventService, ReserveInventoryUseCase reserveInventoryUseCase) {

        super(inboxEventService);

        this.reserveInventoryUseCase = reserveInventoryUseCase;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-service-group")
    public void consume(OrderCreatedEvent event) {

        process(event, event.orderId(), reserveInventoryUseCase::execute);
    }
}