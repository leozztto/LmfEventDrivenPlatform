package com.lmf.platform.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class InboxService {

    private final InboxEventRepository inboxEventRepository;

    public boolean isAlreadyProcessed(String eventId) {

        return inboxEventRepository.findByEventId(eventId)
                .map(inbox -> inbox.getStatus() == InboxStatus.PROCESSED)
                .orElse(false);
    }

    public void register(String eventId, UUID aggregateId, String eventType) {

        try {

            inboxEventRepository.save(new InboxEvent(eventId, aggregateId, eventType));

            log.info("Inbox event registered. eventId={}, aggregateId={}, eventType={}", eventId, aggregateId, eventType);

        } catch (DataIntegrityViolationException exception) {

            throw new DuplicateEventException(eventId);
        }
    }

    public void markProcessed(String eventId) {

        inboxEventRepository.findByEventId(eventId).ifPresent(inbox -> {
            inbox.markProcessed();
            inboxEventRepository.save(inbox);
        });
    }
}
