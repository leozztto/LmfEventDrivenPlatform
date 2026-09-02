package com.lmf.platform.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Serializa o payload do evento e grava a linha do outbox como {@code PENDING}. Deve ser chamado
 * dentro da transação de negócio.
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    public void write(UUID aggregateId, String aggregateType, String eventType, Object payload) {

        try {

            String json = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent(aggregateId, aggregateType, eventType, json);

            outboxEventRepository.save(outboxEvent);

            log.info("Outbox event created. eventId={}, aggregateId={}, eventType={}", outboxEvent.getId(), aggregateId, eventType);

        } catch (JsonProcessingException ex) {

            throw new EventSerializationException("Failed to serialize event of type " + eventType, ex);
        }
    }
}
