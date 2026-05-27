package com.lmf.payment.paymentservice.integration.persistence;

import com.lmf.payment.paymentservice.domain.repository.OutboxEventRepository;
import com.lmf.payment.paymentservice.infrastructure.outbox.OutboxStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.payment.paymentservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void shouldSaveAndFindPendingEvents() {

        OutboxEventEntity outboxEventEntity = new OutboxEventEntity(UUID.randomUUID(), "PAYMENT", "PAYMENT_CREATED", "{\"paymentId\":\"123\"}", OutboxStatus.PENDING);

        outboxEventRepository.save(outboxEventEntity);

        List<OutboxEventEntity> outboxEventEntities = outboxEventRepository.findTop100ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        assertFalse(outboxEventEntities.isEmpty());
        assertEquals(OutboxStatus.PENDING, outboxEventEntities.get(0).getOutboxStatus());
    }
}
