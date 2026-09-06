package com.lmf.auth.authservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthFlowIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @SuppressWarnings("unchecked")
    void registraLogaEConsultaPerfil() {
        String username = "alice-" + UUID.randomUUID();
        String email = username + "@example.com";

        ResponseEntity<Map> register = restTemplate.postForEntity("/api/v1/auth/register",
                json(Map.of("username", username, "email", email, "password", "s3cret123")), Map.class);
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(register.getBody()).containsEntry("username", username);

        ResponseEntity<Map> login = restTemplate.postForEntity("/api/v1/auth/login",
                json(Map.of("usernameOrEmail", username, "password", "s3cret123")), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) login.getBody().get("accessToken");
        assertThat(token).isNotBlank();

        ResponseEntity<Map> jwks = restTemplate.getForEntity("/oauth2/jwks", Map.class);
        assertThat(jwks.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jwks.getBody()).containsKey("keys");

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token);
        ResponseEntity<Map> me = restTemplate.exchange("/api/v1/auth/me", HttpMethod.GET,
                new HttpEntity<>(authHeaders), Map.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody()).containsEntry("username", username);

        ResponseEntity<Map> meNoToken = restTemplate.getForEntity("/api/v1/auth/me", Map.class);
        assertThat(meNoToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> badLogin = restTemplate.postForEntity("/api/v1/auth/login",
                json(Map.of("usernameOrEmail", username, "password", "errada")), Map.class);
        assertThat(badLogin.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private HttpEntity<Map<String, Object>> json(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
