package com.lmf.audit.auditservice.unit.domain;

import com.lmf.audit.auditservice.domain.model.AuditEvent;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEventTest {

    @Test
    void recordGeneratesIdAndReceivedAt() {

        UUID aggregateId = UUID.randomUUID();

        AuditEvent auditEvent = AuditEvent.record("order.created", "event-1", "ORDER_CREATED", aggregateId,
                "correlation-1", "trace-1", "{}");

        assertThat(auditEvent.getId()).isNotNull();
        assertThat(auditEvent.getTopic()).isEqualTo("order.created");
        assertThat(auditEvent.getEventId()).isEqualTo("event-1");
        assertThat(auditEvent.getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(auditEvent.getAggregateId()).isEqualTo(aggregateId);
        assertThat(auditEvent.getCorrelationId()).isEqualTo("correlation-1");
        assertThat(auditEvent.getTraceId()).isEqualTo("trace-1");
        assertThat(auditEvent.getPayload()).isEqualTo("{}");
        assertThat(auditEvent.getReceivedAt()).isNotNull();
    }

    @Test
    void recordAllowsNullCorrelationAndTraceIds() {

        AuditEvent auditEvent = AuditEvent.record("order.created", "event-1", "ORDER_CREATED", UUID.randomUUID(),
                null, null, "{}");

        assertThat(auditEvent.getCorrelationId()).isNull();
        assertThat(auditEvent.getTraceId()).isNull();
    }

    @Test
    void restoreKeepsGivenIdAndReceivedAt() {

        UUID id = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        OffsetDateTime receivedAt = OffsetDateTime.now().minusDays(1);

        AuditEvent auditEvent = AuditEvent.restore(id, "order.created", "event-1", "ORDER_CREATED", aggregateId,
                "correlation-1", "trace-1", "{}", receivedAt);

        assertThat(auditEvent.getId()).isEqualTo(id);
        assertThat(auditEvent.getReceivedAt()).isEqualTo(receivedAt);
    }
}
