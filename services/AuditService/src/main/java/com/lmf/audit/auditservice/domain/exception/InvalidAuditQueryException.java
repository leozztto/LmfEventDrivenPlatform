package com.lmf.audit.auditservice.domain.exception;

public class InvalidAuditQueryException extends RuntimeException {

    public InvalidAuditQueryException(String message) {
        super(message);
    }
}
