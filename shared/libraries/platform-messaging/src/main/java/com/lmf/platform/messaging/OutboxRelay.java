package com.lmf.platform.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Relay do Transactional Outbox: lê as linhas {@code PENDING}, publica no tópico resolvido pelo
 * {@link OutboxTopicRouter} e transiciona o estado. Esgotadas as retentativas, o evento vai para a
 * DLT ({@code platform.outbox.dlt-topic}).
 * <p>
 * Quando a linha carrega um {@code traceParent} (gravado pelo {@link OutboxWriter} no momento da
 * transação de negócio), o publish roda dentro de um span filho desse trace — a instrumentação
 * Observation do {@code KafkaTemplate} (já ligada via {@code spring.kafka.template.observation-enabled})
 * então injeta esse contexto como header no {@code ProducerRecord} automaticamente.
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxRelay {

    private static final String TRACEPARENT_HEADER = "traceparent";

    private final OutboxEventRepository outboxEventRepository;

    private final MessagePublisher messagePublisher;

    private final OutboxTopicRouter topicRouter;

    private final ObjectMapper objectMapper;

    private final String dltTopic;

    private final Tracer tracer;

    private final Propagator propagator;

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

            publishWithTraceContext(event, topic);

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

    private void publishWithTraceContext(OutboxEvent event, String topic) {

        if (event.getTraceParent() == null) {
            messagePublisher.publish(topic, event.getAggregateId().toString(), event.getPayload());
            return;
        }

        Map<String, String> carrier = Map.of(TRACEPARENT_HEADER, event.getTraceParent());
        Span span = propagator.extract(carrier, Map::get).name("outbox-relay").start();

        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            messagePublisher.publish(topic, event.getAggregateId().toString(), event.getPayload());
        } finally {
            span.end();
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
