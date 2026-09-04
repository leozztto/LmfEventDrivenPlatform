package com.lmf.audit.auditservice.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Linha append-only da trilha de auditoria: o envelope de um evento consumido de um dos tópicos da
 * saga, mais os metadados de tracing disponíveis no momento do consumo. Nunca é alterada depois de
 * gravada — o repositório só expõe {@code save} e consultas de leitura.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEvent {

    private UUID id;

    private String topic;

    private String eventId;

    private String eventType;

    private UUID aggregateId;

    private String correlationId;

    private String traceId;

    private String payload;

    private OffsetDateTime receivedAt;

    private AuditEvent(UUID id, String topic, String eventId, String eventType, UUID aggregateId,
                        String correlationId, String traceId, String payload, OffsetDateTime receivedAt) {

        this.id = id;
        this.topic = topic;
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.correlationId = correlationId;
        this.traceId = traceId;
        this.payload = payload;
        this.receivedAt = receivedAt;
    }

    public static AuditEvent record(String topic, String eventId, String eventType, UUID aggregateId,
                                     String correlationId, String traceId, String payload) {

        return new AuditEvent(UUID.randomUUID(), topic, eventId, eventType, aggregateId,
                correlationId, traceId, payload, OffsetDateTime.now());
    }

    public static AuditEvent restore(UUID id, String topic, String eventId, String eventType, UUID aggregateId,
                                      String correlationId, String traceId, String payload, OffsetDateTime receivedAt) {

        return new AuditEvent(id, topic, eventId, eventType, aggregateId, correlationId, traceId, payload, receivedAt);
    }
}
