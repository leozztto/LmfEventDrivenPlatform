package com.lmf.audit.auditservice.infrastructure.web.response;

import com.lmf.audit.auditservice.domain.model.AuditEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditEventResponse(

        UUID id,

        String topic,

        String eventId,

        String eventType,

        UUID aggregateId,

        String correlationId,

        String traceId,

        String payload,

        OffsetDateTime receivedAt) {

    public static AuditEventResponse from(AuditEvent auditEvent) {

        return new AuditEventResponse(
                auditEvent.getId(),
                auditEvent.getTopic(),
                auditEvent.getEventId(),
                auditEvent.getEventType(),
                auditEvent.getAggregateId(),
                auditEvent.getCorrelationId(),
                auditEvent.getTraceId(),
                auditEvent.getPayload(),
                auditEvent.getReceivedAt());
    }
}
