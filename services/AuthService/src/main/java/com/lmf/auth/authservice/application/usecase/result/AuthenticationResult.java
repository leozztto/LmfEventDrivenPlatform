package com.lmf.auth.authservice.application.usecase.result;

public record AuthenticationResult(
        String accessToken,
        String tokenType,
        long expiresInSeconds) {
}
