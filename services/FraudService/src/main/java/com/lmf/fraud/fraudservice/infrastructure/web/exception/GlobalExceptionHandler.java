package com.lmf.fraud.fraudservice.infrastructure.web.exception;

import com.lmf.fraud.fraudservice.domain.exception.BlocklistEntryNotFoundException;
import com.lmf.fraud.fraudservice.domain.exception.InvalidBlocklistEntryException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors().stream().map(this::toFieldError).toList();

        log.warn("Validation error. path={}, fieldErrors={}", request.getRequestURI(), fieldErrors);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(InvalidBlocklistEntryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBlocklistEntry(InvalidBlocklistEntryException ex, HttpServletRequest request) {

        log.warn("Invalid blocklist entry. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_BLOCKLIST_ENTRY", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(BlocklistEntryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBlocklistEntryNotFound(BlocklistEntryNotFoundException ex, HttpServletRequest request) {

        log.warn("Blocklist entry not found. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found. path={}", request.getRequestURI());

        return buildErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found", request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error. path={}", request.getRequestURI(), ex);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Unexpected internal error", request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, String path, List<ErrorResponse.FieldErrorResponse> fieldErrors) {

        ErrorResponse response = new ErrorResponse(Instant.now(), status.value(), error, message, path, fieldErrors);

        return ResponseEntity.status(status).body(response);
    }

    private ErrorResponse.FieldErrorResponse toFieldError(FieldError error) {

        return new ErrorResponse.FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }
}
