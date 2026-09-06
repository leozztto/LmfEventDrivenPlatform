package com.lmf.gateway.gatewayservice.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRolesConverterTest {

    private final JwtRolesConverter converter = new JwtRolesConverter();

    private Jwt jwtWithRoles(Object rolesClaim) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("sub", "alice", "roles", rolesClaim));
    }

    @Test
    void mapeiaClaimRolesParaAuthorities() {
        var authorities = converter.convert(jwtWithRoles(List.of("ROLE_ADMIN", "ROLE_USER")));

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void semRolesRetornaVazio() {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"), Map.of("sub", "alice"));

        assertThat(converter.convert(jwt)).isEmpty();
    }
}
