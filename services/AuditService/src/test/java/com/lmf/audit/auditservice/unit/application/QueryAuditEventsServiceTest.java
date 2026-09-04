package com.lmf.audit.auditservice.unit.application;

import com.lmf.audit.auditservice.application.service.QueryAuditEventsService;
import com.lmf.audit.auditservice.domain.model.AuditEvent;
import com.lmf.audit.auditservice.domain.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QueryAuditEventsServiceTest {

    private final AuditEventRepository auditEventRepository = mock(AuditEventRepository.class);

    private final QueryAuditEventsService queryAuditEventsService = new QueryAuditEventsService(auditEventRepository);

    @Test
    void delegatesQueryByAggregateId() {

        UUID aggregateId = UUID.randomUUID();
        AuditEvent event = AuditEvent.record("order.created", "e1", "ORDER_CREATED", aggregateId, null, null, "{}");
        when(auditEventRepository.findByAggregateId(aggregateId)).thenReturn(List.of(event));

        List<AuditEvent> result = queryAuditEventsService.byAggregateId(aggregateId);

        assertThat(result).containsExactly(event);
    }

    @Test
    void delegatesQueryByCorrelationId() {

        AuditEvent event = AuditEvent.record("order.created", "e1", "ORDER_CREATED", UUID.randomUUID(), "correlation-1", null, "{}");
        when(auditEventRepository.findByCorrelationId("correlation-1")).thenReturn(List.of(event));

        List<AuditEvent> result = queryAuditEventsService.byCorrelationId("correlation-1");

        assertThat(result).containsExactly(event);
    }
}
