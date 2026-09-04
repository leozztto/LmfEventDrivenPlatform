package com.lmf.audit.auditservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.audit.auditservice.application.usecase.RecordAuditEventUseCase;
import com.lmf.audit.auditservice.domain.model.AuditEvent;
import com.lmf.audit.auditservice.domain.repository.AuditEventRepository;
import com.lmf.platform.contracts.EventMessage;
import com.lmf.platform.messaging.EventSerializationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Grava o envelope de um evento consumido de qualquer tópico da saga. O {@code correlationId} só é
 * populado quando presente no MDC — hoje nenhum produtor da plataforma propaga esse valor como header
 * Kafka, então ele fica {@code null} para eventos vindos do broker (ver ADR 0006). O {@code traceId}
 * é populado pela instrumentação Micrometer/Brave do listener Kafka.
 */
@Service
@RequiredArgsConstructor
public class RecordAuditEventService implements RecordAuditEventUseCase {

    private final AuditEventRepository auditEventRepository;

    private final ObjectMapper objectMapper;

    @Override
    public void execute(String topic, EventMessage event, UUID aggregateId) {

        AuditEvent auditEvent = AuditEvent.record(
                topic,
                event.eventId().toString(),
                event.eventType(),
                aggregateId,
                MDC.get("correlationId"),
                MDC.get("traceId"),
                toJson(event));

        auditEventRepository.save(auditEvent);
    }

    private String toJson(EventMessage event) {

        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new EventSerializationException("Failed to serialize event of type " + event.eventType(), ex);
        }
    }
}
