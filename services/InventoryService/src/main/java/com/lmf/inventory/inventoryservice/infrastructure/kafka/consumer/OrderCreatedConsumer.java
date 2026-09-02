package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderCreatedConsumer extends AbstractInboxConsumer<OrderCreatedEvent> {

    private final ReserveInventoryUseCase reserveInventoryUseCase;

    public OrderCreatedConsumer(InboxService inboxService, ReserveInventoryUseCase reserveInventoryUseCase) {

        super(inboxService);

        this.reserveInventoryUseCase = reserveInventoryUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-service-group")
    public void consume(OrderCreatedEvent event) {

        process(event, event.orderId(), reserveInventoryUseCase::execute);
    }
}
