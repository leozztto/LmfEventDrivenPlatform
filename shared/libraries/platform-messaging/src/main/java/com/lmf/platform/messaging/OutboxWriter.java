package com.lmf.platform.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Serializa o payload do evento e grava a linha do outbox como {@code PENDING}. Deve ser chamado
 * dentro da transação de negócio.
 * <p>
 * Também captura o {@code traceparent} (W3C) do span corrente, se houver, para que o
 * {@link OutboxRelay} consiga retomar o mesmo trace ao publicar — sem isso o span da requisição HTTP
 * de origem já teria terminado quando o relay assíncrono rodar.
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxWriter {

    private static final String TRACEPARENT_HEADER = "traceparent";

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    private final Tracer tracer;

    private final Propagator propagator;

    public void write(UUID aggregateId, String aggregateType, String eventType, Object payload) {

        try {

            String json = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent(aggregateId, aggregateType, eventType, json, currentTraceParent());

            outboxEventRepository.save(outboxEvent);

            log.info("Outbox event created. eventId={}, aggregateId={}, eventType={}", outboxEvent.getId(), aggregateId, eventType);

        } catch (JsonProcessingException ex) {

            throw new EventSerializationException("Failed to serialize event of type " + eventType, ex);
        }
    }

    private String currentTraceParent() {

        Span currentSpan = tracer.currentSpan();

        if (currentSpan == null) {
            return null;
        }

        Map<String, String> carrier = new HashMap<>();
        propagator.inject(currentSpan.context(), carrier, Map::put);

        return carrier.get(TRACEPARENT_HEADER);
    }
}
