package com.lmf.auth.authservice.application.usecase.result;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserProfileResult(
        UUID id,
        String username,
        String email,
        List<String> roles,
        boolean enabled,
        OffsetDateTime createdAt) {
}
