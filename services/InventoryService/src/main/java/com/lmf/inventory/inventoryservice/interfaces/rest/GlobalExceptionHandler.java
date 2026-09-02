package com.lmf.inventory.inventoryservice.interfaces.rest;

import com.lmf.inventory.inventoryservice.domain.exception.InsufficientStockException;
import com.lmf.inventory.inventoryservice.domain.exception.InvalidProductException;
import com.lmf.inventory.inventoryservice.domain.exception.InvalidStockException;
import com.lmf.inventory.inventoryservice.domain.exception.ProductAlreadyExistsException;
import com.lmf.inventory.inventoryservice.domain.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ProductNotFoundException ex, HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExists(ProductAlreadyExistsException ex, HttpServletRequest request) {

        return build(HttpStatus.CONFLICT, "PRODUCT_ALREADY_EXISTS", ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {

        return build(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_STOCK", ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidProductException.class, InvalidStockException.class})
    public ResponseEntity<ApiError> handleInvalid(RuntimeException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        log.warn("Validation error. path={}, fieldErrors={}", request.getRequestURI(), fieldErrors);

        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", "Validation failed", request.getRequestURI(), fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error. path={}", request.getRequestURI(), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Unexpected internal error", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message, HttpServletRequest request) {

        log.warn("Request error. status={}, error={}, path={}, message={}", status.value(), error, request.getRequestURI(), message);

        return ResponseEntity.status(status).body(ApiError.of(status.value(), error, message, request.getRequestURI()));
    }
}
