package com.lmf.auth.authservice.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuração da emissão de JWT.
 *
 * <p>{@code publicKey}/{@code privateKey} são opcionais (PEM PKCS#8 sem cabeçalhos, base64 numa
 * linha). Quando ausentes, um par RSA efêmero é gerado no startup — suficiente para o ambiente
 * didático (uma única instância no docker-compose; o Gateway busca a chave pública via JWKS).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    private String issuer = "http://localhost:8087";

    private Duration ttl = Duration.ofHours(1);

    private String publicKey;

    private String privateKey;

    public boolean hasKeyMaterial() {
        return publicKey != null && !publicKey.isBlank()
                && privateKey != null && !privateKey.isBlank();
    }
}
