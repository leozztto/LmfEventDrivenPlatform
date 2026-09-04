package com.lmf.audit.auditservice.infrastructure.persistence.repository;

import com.lmf.audit.auditservice.domain.model.AuditEvent;
import com.lmf.audit.auditservice.domain.repository.AuditEventRepository;
import com.lmf.audit.auditservice.infrastructure.persistence.entity.AuditEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuditEventRepositoryImpl implements AuditEventRepository {

    private final SpringDataAuditEventRepository springDataAuditEventRepository;

    @Override
    public void save(AuditEvent auditEvent) {

        springDataAuditEventRepository.save(toEntity(auditEvent));
    }

    @Override
    public List<AuditEvent> findByAggregateId(UUID aggregateId) {

        return springDataAuditEventRepository.findByAggregateIdOrderByReceivedAtAsc(aggregateId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEvent> findByCorrelationId(String correlationId) {

        return springDataAuditEventRepository.findByCorrelationIdOrderByReceivedAtAsc(correlationId).stream().map(this::toDomain).toList();
    }

    private AuditEventEntity toEntity(AuditEvent auditEvent) {

        return AuditEventEntity.builder()
                .id(auditEvent.getId())
                .topic(auditEvent.getTopic())
                .eventId(auditEvent.getEventId())
                .eventType(auditEvent.getEventType())
                .aggregateId(auditEvent.getAggregateId())
                .correlationId(auditEvent.getCorrelationId())
                .traceId(auditEvent.getTraceId())
                .payload(auditEvent.getPayload())
                .receivedAt(auditEvent.getReceivedAt())
                .build();
    }

    private AuditEvent toDomain(AuditEventEntity entity) {

        return AuditEvent.restore(
                entity.getId(),
                entity.getTopic(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getAggregateId(),
                entity.getCorrelationId(),
                entity.getTraceId(),
                entity.getPayload(),
                entity.getReceivedAt());
    }
}
