package com.lmf.auth.authservice.infrastructure.web.controller;

import com.nimbusds.jose.jwk.JWKSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Publica o JWK Set com a(s) chave(s) <b>pública(s)</b> usada(s) para assinar os JWT. O Gateway
 * consome este endpoint para validar tokens localmente, sem chamar o AuthService a cada request.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "JWKS", description = "Chaves públicas para validação de JWT")
public class JwksController {

    private final JWKSet jwkSet;

    @Operation(summary = "JWK Set público (RFC 7517)")
    @GetMapping("/oauth2/jwks")
    public Map<String, Object> keys() {
        return jwkSet.toPublicJWKSet().toJSONObject();
    }
}
