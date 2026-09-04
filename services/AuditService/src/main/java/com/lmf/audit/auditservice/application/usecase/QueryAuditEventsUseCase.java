package com.lmf.audit.auditservice.application.usecase;

import com.lmf.audit.auditservice.domain.model.AuditEvent;

import java.util.List;
import java.util.UUID;

public interface QueryAuditEventsUseCase {

    List<AuditEvent> byAggregateId(UUID aggregateId);

    List<AuditEvent> byCorrelationId(String correlationId);
}
