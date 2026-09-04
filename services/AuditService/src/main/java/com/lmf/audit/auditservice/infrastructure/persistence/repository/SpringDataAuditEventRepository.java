package com.lmf.audit.auditservice.infrastructure.persistence.repository;

import com.lmf.audit.auditservice.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    List<AuditEventEntity> findByAggregateIdOrderByReceivedAtAsc(UUID aggregateId);

    List<AuditEventEntity> findByCorrelationIdOrderByReceivedAtAsc(String correlationId);
}
