package com.lmf.audit.auditservice.unit.infrastructure.web;

import com.lmf.audit.auditservice.application.usecase.QueryAuditEventsUseCase;
import com.lmf.audit.auditservice.domain.model.AuditEvent;
import com.lmf.audit.auditservice.infrastructure.web.controller.AuditEventController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditEventController.class)
class AuditEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueryAuditEventsUseCase queryAuditEventsUseCase;

    @Test
    @DisplayName("Should query by aggregateId")
    void shouldQueryByAggregateId() throws Exception {

        UUID aggregateId = UUID.randomUUID();
        AuditEvent event = AuditEvent.record("order.created", "e1", "ORDER_CREATED", aggregateId, null, null, "{}");

        when(queryAuditEventsUseCase.byAggregateId(aggregateId)).thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/audit-events").param("aggregateId", aggregateId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("ORDER_CREATED"))
                .andExpect(jsonPath("$[0].aggregateId").value(aggregateId.toString()));
    }

    @Test
    @DisplayName("Should query by correlationId")
    void shouldQueryByCorrelationId() throws Exception {

        AuditEvent event = AuditEvent.record("order.created", "e1", "ORDER_CREATED", UUID.randomUUID(), "correlation-1", null, "{}");

        when(queryAuditEventsUseCase.byCorrelationId("correlation-1")).thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/audit-events").param("correlationId", "correlation-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correlationId").value("correlation-1"));
    }

    @Test
    @DisplayName("Should return bad request when no filter is given")
    void shouldReturnBadRequestWhenNoFilterGiven() throws Exception {

        mockMvc.perform(get("/api/v1/audit-events"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_AUDIT_QUERY"));
    }

    @Test
    @DisplayName("Should return bad request when both filters are given")
    void shouldReturnBadRequestWhenBothFiltersGiven() throws Exception {

        mockMvc.perform(get("/api/v1/audit-events")
                        .param("aggregateId", UUID.randomUUID().toString())
                        .param("correlationId", "correlation-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_AUDIT_QUERY"));
    }
}
