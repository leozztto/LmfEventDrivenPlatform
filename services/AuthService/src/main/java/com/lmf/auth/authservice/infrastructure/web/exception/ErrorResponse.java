package com.lmf.auth.authservice.infrastructure.web.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(

        Instant timestamp,

        Integer status,

        String error,

        String message,

        String path,

        List<FieldErrorResponse> fieldErrors) {

    public record FieldErrorResponse(

            String field,

            String message) {
    }
}
