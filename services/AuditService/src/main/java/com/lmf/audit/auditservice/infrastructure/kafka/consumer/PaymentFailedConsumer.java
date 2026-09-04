package com.lmf.audit.auditservice.infrastructure.kafka.consumer;

import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.PaymentFailedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentFailedConsumer extends AbstractInboxConsumer<PaymentFailedEvent> {

    private final RecordAuditEventUseCase recordAuditEventUseCase;

    public PaymentFailedConsumer(InboxService inboxService, RecordAuditEventUseCase recordAuditEventUseCase) {

        super(inboxService);

        this.recordAuditEventUseCase = recordAuditEventUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = KafkaTopics.GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.PaymentFailedEvent")
    public void consume(PaymentFailedEvent event) {

        process(event, event.orderId(), e -> recordAuditEventUseCase.execute(KafkaTopics.PAYMENT_FAILED, e, e.orderId()));
    }
}
