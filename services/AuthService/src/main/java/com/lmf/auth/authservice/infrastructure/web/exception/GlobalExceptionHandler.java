package com.lmf.auth.authservice.infrastructure.web.exception;

import com.lmf.auth.authservice.domain.exception.DisabledUserException;
import com.lmf.auth.authservice.domain.exception.EmailAlreadyExistsException;
import com.lmf.auth.authservice.domain.exception.InvalidCredentialsException;
import com.lmf.auth.authservice.domain.exception.InvalidEmailException;
import com.lmf.auth.authservice.domain.exception.UserNotFoundException;
import com.lmf.auth.authservice.domain.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
        List<ErrorResponse.FieldErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError).toList();
        log.warn("Erro de validação. path={}, fieldErrors={}", request.getRequestURI(), fieldErrors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler({UsernameAlreadyExistsException.class, EmailAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleAlreadyExists(RuntimeException ex, HttpServletRequest request) {
        log.warn("Cadastro duplicado. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        log.warn("Falha de login. path={}", request.getRequestURI());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DisabledUserException.class)
    public ResponseEntity<ErrorResponse> handleDisabledUser(DisabledUserException ex, HttpServletRequest request) {
        log.warn("Usuário desabilitado. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "USER_DISABLED", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("Usuário não encontrado. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmail(InvalidEmailException ex, HttpServletRequest request) {
        log.warn("E-mail inválido. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_EMAIL", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade (provável cadastro concorrente). path={}", request.getRequestURI());
        return buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", "Requisição concorrente conflitante; tente novamente", request.getRequestURI(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acesso negado. path={}", request.getRequestURI());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Acesso negado", request.getRequestURI(), null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("Recurso não encontrado. path={}", request.getRequestURI());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found", request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado. path={}", request.getRequestURI(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Unexpected internal error", request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, String path,
                                                             List<ErrorResponse.FieldErrorResponse> fieldErrors) {
        ErrorResponse response = new ErrorResponse(Instant.now(), status.value(), error, message, path, fieldErrors);
        return ResponseEntity.status(status).body(response);
    }

    private ErrorResponse.FieldErrorResponse toFieldError(FieldError error) {
        return new ErrorResponse.FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }
}
