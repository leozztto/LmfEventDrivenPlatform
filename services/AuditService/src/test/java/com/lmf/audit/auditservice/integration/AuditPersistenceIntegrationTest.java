package com.lmf.audit.auditservice.integration;

import com.lmf.audit.auditservice.domain.model.AuditEvent;
import com.lmf.audit.auditservice.domain.repository.AuditEventRepository;
import com.lmf.audit.auditservice.infrastructure.persistence.repository.SpringDataAuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Round-trip real pelo Postgres: prova que o mapper manual do adapter de persistência (domínio
 * &lt;-&gt; entidade JPA) preserva todos os campos, incluindo os nulos de correlationId/traceId.
 */
class AuditPersistenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private SpringDataAuditEventRepository springDataAuditEventRepository;

    @BeforeEach
    void clean() {
        springDataAuditEventRepository.deleteAll();
    }

    @Test
    void auditEventRoundTrips() {

        UUID aggregateId = UUID.randomUUID();
        AuditEvent auditEvent = AuditEvent.record("order.created", "event-1", "ORDER_CREATED", aggregateId,
                "correlation-1", "trace-1", "{\"orderId\":\"" + aggregateId + "\"}");

        auditEventRepository.save(auditEvent);

        assertThat(auditEventRepository.findByAggregateId(aggregateId)).singleElement().satisfies(saved -> {
            assertThat(saved.getId()).isEqualTo(auditEvent.getId());
            assertThat(saved.getTopic()).isEqualTo("order.created");
            assertThat(saved.getEventId()).isEqualTo("event-1");
            assertThat(saved.getEventType()).isEqualTo("ORDER_CREATED");
            assertThat(saved.getCorrelationId()).isEqualTo("correlation-1");
            assertThat(saved.getTraceId()).isEqualTo("trace-1");
            assertThat(saved.getPayload()).contains(aggregateId.toString());
            assertThat(saved.getReceivedAt()).isNotNull();
        });
    }

    @Test
    void auditEventRoundTripsWithNullCorrelationAndTraceIds() {

        UUID aggregateId = UUID.randomUUID();
        AuditEvent auditEvent = AuditEvent.record("payment.failed", "event-2", "PAYMENT_FAILED", aggregateId, null, null, "{}");

        auditEventRepository.save(auditEvent);

        assertThat(auditEventRepository.findByAggregateId(aggregateId)).singleElement().satisfies(saved -> {
            assertThat(saved.getCorrelationId()).isNull();
            assertThat(saved.getTraceId()).isNull();
        });
    }

    @Test
    void findsByCorrelationIdAcrossDifferentAggregates() {

        AuditEvent first = AuditEvent.record("order.created", "e1", "ORDER_CREATED", UUID.randomUUID(), "shared-correlation", null, "{}");
        AuditEvent second = AuditEvent.record("fraud.approved", "e2", "FRAUD_APPROVED", UUID.randomUUID(), "shared-correlation", null, "{}");
        AuditEvent unrelated = AuditEvent.record("order.created", "e3", "ORDER_CREATED", UUID.randomUUID(), "other-correlation", null, "{}");

        auditEventRepository.save(first);
        auditEventRepository.save(second);
        auditEventRepository.save(unrelated);

        assertThat(auditEventRepository.findByCorrelationId("shared-correlation"))
                .extracting(AuditEvent::getEventId)
                .containsExactlyInAnyOrder("e1", "e2");
    }
}
