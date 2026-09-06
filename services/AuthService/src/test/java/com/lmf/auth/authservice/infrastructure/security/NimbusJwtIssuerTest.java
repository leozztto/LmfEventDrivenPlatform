package com.lmf.auth.authservice.infrastructure.security;

import com.lmf.auth.authservice.application.port.TokenIssuer;
import com.lmf.auth.authservice.domain.model.user.Email;
import com.lmf.auth.authservice.domain.model.user.Role;
import com.lmf.auth.authservice.domain.model.user.User;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class NimbusJwtIssuerTest {

    private NimbusJwtIssuer issuer;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() throws Exception {
        RSAKey rsaKey = RsaKeys.generate();

        JwtProperties properties = new JwtProperties();
        properties.setIssuer("http://auth-test");
        properties.setTtl(Duration.ofMinutes(30));

        issuer = new NimbusJwtIssuer(new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey))), properties);
        decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Test
    void emiteTokenComClaimsEsperados() {
        User user = new User("alice", new Email("alice@example.com"), "hash",
                Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));

        TokenIssuer.IssuedToken issued = issuer.issue(user);
        Jwt jwt = decoder.decode(issued.token());

        assertThat(jwt.getSubject()).isEqualTo("alice");
        assertThat(jwt.getIssuer()).hasToString("http://auth-test");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(jwt.<String>getClaim("email")).isEqualTo("alice@example.com");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getExpiresAt()).isCloseTo(Instant.now().plus(Duration.ofMinutes(30)),
                within(30, ChronoUnit.SECONDS));
    }
}
