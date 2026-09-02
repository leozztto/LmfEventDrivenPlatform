package com.lmf.platform.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Relay do Transactional Outbox: lê as linhas {@code PENDING}, publica no tópico resolvido pelo
 * {@link OutboxTopicRouter} e transiciona o estado. Esgotadas as retentativas, o evento vai para a
 * DLT ({@code platform.outbox.dlt-topic}).
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventRepository outboxEventRepository;

    private final MessagePublisher messagePublisher;

    private final OutboxTopicRouter topicRouter;

    private final ObjectMapper objectMapper;

    private final String dltTopic;

    @Scheduled(fixedDelayString = "${platform.outbox.poll-interval-ms:5000}")
    @Transactional
    public void process() {

        List<OutboxEvent> pending = outboxEventRepository.lockPending(OutboxStatus.PENDING, Limit.of(100));

        if (pending.isEmpty()) {
            return;
        }

        log.info("Relaying outbox events. batchSize={}", pending.size());

        for (OutboxEvent event : pending) {
            relay(event);
        }
    }

    private void relay(OutboxEvent event) {

        try {

            String topic = topicRouter.topicFor(event.getEventType());

            event.markProcessing();
            outboxEventRepository.saveAndFlush(event);

            messagePublisher.publish(topic, event.getAggregateId().toString(), event.getPayload());

            event.markPublished();
            outboxEventRepository.saveAndFlush(event);

            log.info("Outbox event published. eventId={}, eventType={}, topic={}", event.getId(), event.getEventType(), topic);

        } catch (Exception ex) {

            event.markFailed(ex.getMessage());
            outboxEventRepository.saveAndFlush(event);

            log.warn("Outbox event relay failed. eventId={}, retryCount={}, error={}", event.getId(), event.getRetryCount(), ex.getMessage());

            if (event.getStatus() == OutboxStatus.DLT) {

                messagePublisher.publish(dltTopic, event.getAggregateId().toString(), toJson(DltEvent.from(event)));
                log.error("Outbox event moved to DLT. eventId={}, dltTopic={}", event.getId(), dltTopic);

            } else {

                event.markPendingRetry();
                outboxEventRepository.saveAndFlush(event);
            }
        }
    }

    private String toJson(DltEvent dltEvent) {
        try {
            return objectMapper.writeValueAsString(dltEvent);
        } catch (JsonProcessingException ex) {
            return "{\"eventId\":\"" + dltEvent.eventId() + "\",\"error\":\"dlt-serialization-failed\"}";
        }
    }
}
