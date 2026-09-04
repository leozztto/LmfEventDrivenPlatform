package com.lmf.audit.auditservice.infrastructure.kafka.consumer;

import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FraudApprovedConsumer extends AbstractInboxConsumer<FraudApprovedEvent> {

    private final RecordAuditEventUseCase recordAuditEventUseCase;

    public FraudApprovedConsumer(InboxService inboxService, RecordAuditEventUseCase recordAuditEventUseCase) {

        super(inboxService);

        this.recordAuditEventUseCase = recordAuditEventUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.FRAUD_APPROVED, groupId = KafkaTopics.GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.FraudApprovedEvent")
    public void consume(FraudApprovedEvent event) {

        process(event, event.orderId(), e -> recordAuditEventUseCase.execute(KafkaTopics.FRAUD_APPROVED, e, e.orderId()));
    }
}
