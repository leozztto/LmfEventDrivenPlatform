package com.lmf.audit.auditservice.infrastructure.kafka.consumer;

import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Terceiro leitor de {@code order.created} (depois de FraudService e NotificationService) — só
 * grava a trilha de auditoria, não participa da saga.
 */
@Component
public class OrderCreatedConsumer extends AbstractInboxConsumer<OrderCreatedEvent> {

    private final RecordAuditEventUseCase recordAuditEventUseCase;

    public OrderCreatedConsumer(InboxService inboxService, RecordAuditEventUseCase recordAuditEventUseCase) {

        super(inboxService);

        this.recordAuditEventUseCase = recordAuditEventUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = KafkaTopics.GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.OrderCreatedEvent")
    public void consume(OrderCreatedEvent event) {

        process(event, event.orderId(), e -> recordAuditEventUseCase.execute(KafkaTopics.ORDER_CREATED, e, e.orderId()));
    }
}
