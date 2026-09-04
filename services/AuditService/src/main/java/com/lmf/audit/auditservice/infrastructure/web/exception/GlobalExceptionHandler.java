package com.lmf.audit.auditservice.infrastructure.web.exception;

import com.lmf.audit.auditservice.domain.exception.InvalidAuditQueryException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidAuditQueryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAuditQuery(InvalidAuditQueryException ex, HttpServletRequest request) {

        log.warn("Invalid audit query. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_AUDIT_QUERY", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found. path={}", request.getRequestURI());

        return buildErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error. path={}", request.getRequestURI(), ex);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Unexpected internal error", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, String path) {

        ErrorResponse response = new ErrorResponse(Instant.now(), status.value(), error, message, path);

        return ResponseEntity.status(status).body(response);
    }
}
