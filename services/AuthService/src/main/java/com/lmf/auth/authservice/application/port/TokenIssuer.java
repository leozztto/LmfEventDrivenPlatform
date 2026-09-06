package com.lmf.auth.authservice.application.port;

import com.lmf.auth.authservice.domain.model.user.User;

import java.time.Instant;

/**
 * Porta de emissão de token de acesso. A implementação (JWT assinado com RSA) fica na
 * infraestrutura.
 */
public interface TokenIssuer {

    IssuedToken issue(User user);

    record IssuedToken(String token, Instant issuedAt, Instant expiresAt) {
    }
}
