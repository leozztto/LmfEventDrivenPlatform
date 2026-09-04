package com.lmf.audit.auditservice.application.usecase;

import com.lmf.platform.contracts.EventMessage;

import java.util.UUID;

public interface RecordAuditEventUseCase {

    void execute(String topic, EventMessage event, UUID aggregateId);
}
