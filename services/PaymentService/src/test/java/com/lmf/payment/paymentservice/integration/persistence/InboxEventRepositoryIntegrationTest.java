package com.lmf.payment.paymentservice.integration.persistence;

import com.lmf.payment.paymentservice.domain.repository.InboxEventRepository;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.InboxEventEntity;
import com.lmf.payment.paymentservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InboxEventRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private InboxEventRepository inboxEventRepository;

    @Test
    void shouldSaveAndFindInboxEvent() {

        InboxEventEntity inboxEventEntity = new InboxEventEntity("event-123", UUID.randomUUID(), "ORDER_CREATED");

        inboxEventRepository.save(inboxEventEntity);

        Optional<InboxEventEntity> optionalInboxEventEntity = inboxEventRepository.findByEventId("event-123");

        assertTrue(optionalInboxEventEntity.isPresent());
        assertEquals("ORDER_CREATED", optionalInboxEventEntity.get().getEventType());
    }

    @Test
    void shouldReturnTrueWhenEventExists() {

        InboxEventEntity inboxEventEntity = new InboxEventEntity("event-456", UUID.randomUUID(), "ORDER_CREATED");

        inboxEventRepository.save(inboxEventEntity);

        boolean exists = inboxEventRepository.existsByEventId("event-456");

        assertTrue(exists);
    }
}
