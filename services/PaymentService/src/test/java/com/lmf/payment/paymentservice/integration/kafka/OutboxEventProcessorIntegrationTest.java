package com.lmf.payment.paymentservice.integration.kafka;

import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import com.lmf.payment.paymentservice.infrastructure.kafka.outbox.OutboxEventProcessor;
import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.payment.paymentservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutboxEventProcessorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxEventProcessor outboxEventProcessor;

    @Test
    void shouldProcessPendingOutboxEvent() {

        OutboxEventEntity outboxEventEntity = new OutboxEventEntity(UUID.randomUUID(), "PAYMENT", "PAYMENT_CREATED", "{\"paymentId\":\"123\"}", OutboxStatus.PENDING);

        outboxEventRepository.save(outboxEventEntity);

        outboxEventProcessor.process();

        OutboxEventEntity updated = outboxEventRepository.findById(outboxEventEntity.getId()).orElseThrow();

        assertEquals(OutboxStatus.PUBLISHED, updated.getOutboxStatus());
    }
}
