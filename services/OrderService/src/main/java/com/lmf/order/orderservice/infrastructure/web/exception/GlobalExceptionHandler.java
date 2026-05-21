package com.lmf.order.orderservice.infrastructure.web.exception;

import com.lmf.order.orderservice.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
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

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {

        log.warn("Business error. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_ERROR", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, HttpServletRequest request) {

        log.warn("Order not found. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(EmptyOrderException.class)
    public ResponseEntity<ErrorResponse> handleEmptyOrder(EmptyOrderException ex, HttpServletRequest request) {

        log.warn("Empty order error. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "EMPTY_ORDER", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStatus(InvalidOrderStatusException ex, HttpServletRequest request) {

        log.warn("Invalid order status. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.CONFLICT, "INVALID_ORDER_STATUS", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<ErrorResponse> handleInvalidQuantityException(InvalidQuantityException ex, HttpServletRequest request) {

        log.warn("Invalid quantity. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_QUANTITY", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidUnitPriceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUnitPriceException(InvalidUnitPriceException ex, HttpServletRequest request) {

        log.warn("Invalid unit price. path={}, message={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_UNIT_PRICE", ex.getMessage(), request.getRequestURI(), null);
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

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {

        log.warn("Missing request header. path={}, header={}", request.getRequestURI(), ex.getHeaderName());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "MISSING_HEADER", ex.getMessage(), request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, String path, List<ErrorResponse.FieldErrorResponse> fieldErrors) {

        ErrorResponse response = new ErrorResponse(Instant.now(), status.value(), error, message, path, fieldErrors);

        return ResponseEntity.status(status).body(response);
    }

    private ErrorResponse.FieldErrorResponse toFieldError(FieldError error) {

        return new ErrorResponse.FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }
}
