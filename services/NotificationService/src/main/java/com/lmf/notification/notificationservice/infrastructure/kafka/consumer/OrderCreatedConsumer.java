package com.lmf.notification.notificationservice.infrastructure.kafka.consumer;

import com.lmf.notification.notificationservice.application.usecase.NotifyOrderCreatedUseCase;
import com.lmf.notification.notificationservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Segundo consumidor de {@code order.created} (o primeiro é o InventoryService). Guarda o contato
 * do cliente e dispara a notificação de "pedido recebido".
 */
@Component
public class OrderCreatedConsumer extends AbstractInboxConsumer<OrderCreatedEvent> {

    private final NotifyOrderCreatedUseCase notifyOrderCreatedUseCase;

    public OrderCreatedConsumer(InboxService inboxService, NotifyOrderCreatedUseCase notifyOrderCreatedUseCase) {

        super(inboxService);

        this.notifyOrderCreatedUseCase = notifyOrderCreatedUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = KafkaTopics.GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.OrderCreatedEvent")
    public void consume(OrderCreatedEvent event) {

        process(event, event.orderId(), notifyOrderCreatedUseCase::execute);
    }
}
