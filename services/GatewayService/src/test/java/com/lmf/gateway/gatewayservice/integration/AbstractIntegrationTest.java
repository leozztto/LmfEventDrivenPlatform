package com.lmf.gateway.gatewayservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "gateway.ratelimit.limit-for-period=5")
public abstract class AbstractIntegrationTest {

    protected static final RSAKey RSA_KEY = generateKey();
    protected static final WireMockServer WIREMOCK = new WireMockServer(wireMockConfig().dynamicPort());
    private static final NimbusJwtEncoder ENCODER =
            new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(RSA_KEY)));

    static {
        WIREMOCK.start();
    }

    private static RSAKey generateKey() {
        try {
            return new RSAKeyGenerator(2048).keyID("test-key").generate();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String baseUrl = WIREMOCK.baseUrl();
        registry.add("AUTH_SERVICE_URL", () -> baseUrl);
        registry.add("ORDER_SERVICE_URL", () -> baseUrl);
        registry.add("INVENTORY_SERVICE_URL", () -> baseUrl);
        registry.add("FRAUD_SERVICE_URL", () -> baseUrl);
        registry.add("AUDIT_SERVICE_URL", () -> baseUrl);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> baseUrl + "/oauth2/jwks");
    }

    @BeforeEach
    void resetWireMock() {
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo("/oauth2/jwks")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(new JWKSet(RSA_KEY).toPublicJWKSet().toString())));
    }

    protected String mintToken(String subject, String... roles) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8087")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .claim("roles", List.of(roles))
                .build();
        return ENCODER.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
    }
}
