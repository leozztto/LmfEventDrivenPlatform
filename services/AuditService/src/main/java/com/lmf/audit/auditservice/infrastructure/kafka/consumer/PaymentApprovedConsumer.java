package com.lmf.audit.auditservice.infrastructure.kafka.consumer;

import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentApprovedConsumer extends AbstractInboxConsumer<PaymentApprovedEvent> {

    private final RecordAuditEventUseCase recordAuditEventUseCase;

    public PaymentApprovedConsumer(InboxService inboxService, RecordAuditEventUseCase recordAuditEventUseCase) {

        super(inboxService);

        this.recordAuditEventUseCase = recordAuditEventUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.PAYMENT_APPROVED, groupId = KafkaTopics.GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.PaymentApprovedEvent")
    public void consume(PaymentApprovedEvent event) {

        process(event, event.orderId(), e -> recordAuditEventUseCase.execute(KafkaTopics.PAYMENT_APPROVED, e, e.orderId()));
    }
}
