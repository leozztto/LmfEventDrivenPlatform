package com.lmf.notification.notificationservice.infrastructure.kafka.consumer;

import com.lmf.notification.notificationservice.application.usecase.NotifyInventoryReservationFailedUseCase;
import com.lmf.notification.notificationservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryReservationFailedConsumer extends AbstractInboxConsumer<InventoryReservationFailedEvent> {

    private final NotifyInventoryReservationFailedUseCase notifyInventoryReservationFailedUseCase;

    public InventoryReservationFailedConsumer(InboxService inboxService, NotifyInventoryReservationFailedUseCase notifyInventoryReservationFailedUseCase) {

        super(inboxService);

        this.notifyInventoryReservationFailedUseCase = notifyInventoryReservationFailedUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVATION_FAILED, groupId = KafkaTopics.GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.InventoryReservationFailedEvent")
    public void consume(InventoryReservationFailedEvent event) {

        process(event, event.orderId(), notifyInventoryReservationFailedUseCase::execute);
    }
}
