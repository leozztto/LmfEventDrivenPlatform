package com.lmf.audit.auditservice.unit.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lmf.audit.auditservice.Fixtures;
import com.lmf.audit.auditservice.application.service.RecordAuditEventService;
import com.lmf.audit.auditservice.domain.model.AuditEvent;
import com.lmf.audit.auditservice.domain.repository.AuditEventRepository;
import com.lmf.audit.auditservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.OrderCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RecordAuditEventServiceTest {

    private final AuditEventRepository auditEventRepository = mock(AuditEventRepository.class);

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final RecordAuditEventService recordAuditEventService = new RecordAuditEventService(auditEventRepository, objectMapper);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void savesAuditEventWithSerializedPayload() {

        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, UUID.randomUUID());

        recordAuditEventService.execute(KafkaTopics.ORDER_CREATED, event, orderId);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());

        AuditEvent saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo(KafkaTopics.ORDER_CREATED);
        assertThat(saved.getEventId()).isEqualTo(event.eventId().toString());
        assertThat(saved.getEventType()).isEqualTo(OrderCreatedEvent.TYPE);
        assertThat(saved.getAggregateId()).isEqualTo(orderId);
        assertThat(saved.getPayload()).contains(orderId.toString()).contains(OrderCreatedEvent.TYPE);
        assertThat(saved.getCorrelationId()).isNull();
        assertThat(saved.getTraceId()).isNull();
    }

    @Test
    void capturesCorrelationAndTraceIdsFromMdcWhenPresent() {

        MDC.put("correlationId", "correlation-123");
        MDC.put("traceId", "trace-456");

        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, UUID.randomUUID());

        recordAuditEventService.execute(KafkaTopics.ORDER_CREATED, event, orderId);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());

        assertThat(captor.getValue().getCorrelationId()).isEqualTo("correlation-123");
        assertThat(captor.getValue().getTraceId()).isEqualTo("trace-456");
    }
}
