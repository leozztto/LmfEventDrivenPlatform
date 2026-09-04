package com.lmf.audit.auditservice.infrastructure.web.controller;

import com.lmf.audit.auditservice.application.usecase.QueryAuditEventsUseCase;
import com.lmf.audit.auditservice.domain.exception.InvalidAuditQueryException;
import com.lmf.audit.auditservice.infrastructure.web.response.AuditEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Consulta de leitura da trilha de auditoria — exige exatamente um dos dois filtros. Sem paginação
 * nem autenticação, escopo mínimo para a v1.
 */
@RestController
@RequestMapping("/api/v1/audit-events")
@RequiredArgsConstructor
public class AuditEventController {

    private final QueryAuditEventsUseCase queryAuditEventsUseCase;

    @GetMapping
    public List<AuditEventResponse> query(@RequestParam(required = false) UUID aggregateId,
                                           @RequestParam(required = false) String correlationId) {

        if (aggregateId != null && correlationId != null) {
            throw new InvalidAuditQueryException("Informe apenas um dos filtros: aggregateId ou correlationId");
        }

        if (aggregateId != null) {
            return queryAuditEventsUseCase.byAggregateId(aggregateId).stream().map(AuditEventResponse::from).toList();
        }

        if (correlationId != null) {
            return queryAuditEventsUseCase.byCorrelationId(correlationId).stream().map(AuditEventResponse::from).toList();
        }

        throw new InvalidAuditQueryException("Informe um dos filtros: aggregateId ou correlationId");
    }
}
