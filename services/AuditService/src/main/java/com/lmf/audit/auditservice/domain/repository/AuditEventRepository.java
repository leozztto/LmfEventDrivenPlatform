package com.lmf.audit.auditservice.domain.repository;

import com.lmf.audit.auditservice.domain.model.AuditEvent;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository {

    void save(AuditEvent auditEvent);

    List<AuditEvent> findByAggregateId(UUID aggregateId);

    List<AuditEvent> findByCorrelationId(String correlationId);
}
