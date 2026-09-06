package com.lmf.auth.authservice.infrastructure.web.response;

import com.lmf.auth.authservice.application.usecase.result.UserProfileResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String email,
        List<String> roles,
        boolean enabled,
        OffsetDateTime createdAt) {

    public static UserProfileResponse from(UserProfileResult result) {
        return new UserProfileResponse(result.id(), result.username(), result.email(), result.roles(),
                result.enabled(), result.createdAt());
    }
}
