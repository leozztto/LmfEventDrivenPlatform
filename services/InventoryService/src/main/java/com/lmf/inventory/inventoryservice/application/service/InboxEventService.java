package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.domain.exception.DuplicateEventException;
import com.lmf.inventory.inventoryservice.domain.repository.InboxEventRepository;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.InboxEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboxEventService {

    private final InboxEventRepository inboxEventRepository;

    public boolean isDuplicate(String eventId) {

        boolean duplicated = inboxEventRepository.existsByEventId(eventId);

        if (duplicated) {

            log.warn("Duplicate event detected. eventId={}", eventId);
        }

        return duplicated;
    }

    @Transactional
    public InboxEventEntity register(String eventId, UUID aggregateId, String eventType) {

        try {

            InboxEventEntity inboxMessage = new InboxEventEntity(eventId, aggregateId, eventType);

            InboxEventEntity saved = inboxEventRepository.save(inboxMessage);

            log.info("Inbox event registered. eventId={}, aggregateId={}, eventType={}", eventId, aggregateId, eventType);

            return saved;

        } catch (DataIntegrityViolationException exception) {

            throw new DuplicateEventException(eventId);
        }
    }

    public void markProcessed(String eventId) {

        inboxEventRepository.findByEventId(eventId).ifPresent(inbox -> {

            inbox.markProcessed();

            inboxEventRepository.save(inbox);

            log.info("Inbox event processed successfully. eventId={}", eventId);
        });
    }

    public void markFailed(String eventId, String reason) {

        inboxEventRepository.findByEventId(eventId).ifPresent(inbox -> {

            inbox.markFailed(reason);

            inboxEventRepository.save(inbox);

            log.error("Inbox event processing failed. eventId={}, reason={}", eventId, reason);
        });
    }
}
