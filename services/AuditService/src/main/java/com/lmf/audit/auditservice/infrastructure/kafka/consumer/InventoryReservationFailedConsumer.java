package com.lmf.audit.auditservice.infrastructure.kafka.consumer;

import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryReservationFailedConsumer extends AbstractInboxConsumer<InventoryReservationFailedEvent> {

    private final RecordAuditEventUseCase recordAuditEventUseCase;

    public InventoryReservationFailedConsumer(InboxService inboxService, RecordAuditEventUseCase recordAuditEventUseCase) {

        super(inboxService);

        this.recordAuditEventUseCase = recordAuditEventUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVATION_FAILED, groupId = KafkaTopics.GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.InventoryReservationFailedEvent")
    public void consume(InventoryReservationFailedEvent event) {

        process(event, event.orderId(), e -> recordAuditEventUseCase.execute(KafkaTopics.INVENTORY_RESERVATION_FAILED, e, e.orderId()));
    }
}
