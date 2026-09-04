package com.lmf.audit.auditservice.application.service;

import com.lmf.audit.auditservice.application.usecase.QueryAuditEventsUseCase;
import com.lmf.audit.auditservice.domain.model.AuditEvent;
import com.lmf.audit.auditservice.domain.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryAuditEventsService implements QueryAuditEventsUseCase {

    private final AuditEventRepository auditEventRepository;

    @Override
    public List<AuditEvent> byAggregateId(UUID aggregateId) {
        return auditEventRepository.findByAggregateId(aggregateId);
    }

    @Override
    public List<AuditEvent> byCorrelationId(String correlationId) {
        return auditEventRepository.findByCorrelationId(correlationId);
    }
}
