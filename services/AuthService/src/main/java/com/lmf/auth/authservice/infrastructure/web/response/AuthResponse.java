package com.lmf.auth.authservice.infrastructure.web.response;

import com.lmf.auth.authservice.application.usecase.result.AuthenticationResult;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {

    public static AuthResponse from(AuthenticationResult result) {
        return new AuthResponse(result.accessToken(), result.tokenType(), result.expiresInSeconds());
    }
}
