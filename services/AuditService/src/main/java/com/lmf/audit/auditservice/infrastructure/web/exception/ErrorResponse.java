package com.lmf.audit.auditservice.infrastructure.web.exception;

import java.time.Instant;

public record ErrorResponse(

        Instant timestamp,

        Integer status,

        String error,

        String message,

        String path) {
}
