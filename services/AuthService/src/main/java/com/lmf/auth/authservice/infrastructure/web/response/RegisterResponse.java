package com.lmf.auth.authservice.infrastructure.web.response;

import com.lmf.auth.authservice.application.usecase.result.RegisterUserResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String username,
        String email,
        List<String> roles,
        OffsetDateTime createdAt) {

    public static RegisterResponse from(RegisterUserResult result) {
        return new RegisterResponse(result.id(), result.username(), result.email(), result.roles(),
                result.createdAt());
    }
}
