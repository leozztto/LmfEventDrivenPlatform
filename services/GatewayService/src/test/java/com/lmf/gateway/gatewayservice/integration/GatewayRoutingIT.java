package com.lmf.gateway.gatewayservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

class GatewayRoutingIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private void stubDownstream() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v1/orders/x")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("{\"id\":\"x\"}")));
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v1/audit-events")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("[]")));
        WIREMOCK.stubFor(post(urlPathEqualTo("/api/v1/auth/login")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("{\"accessToken\":\"stub\"}")));
        WIREMOCK.stubFor(get(urlPathEqualTo("/v3/api-docs")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("{\"openapi\":\"3.0.1\"}")));
    }

    private HttpEntity<Void> bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    private ResponseEntity<String> authGet(String path, HttpEntity<Void> entity) {
        return restTemplate.exchange(path, HttpMethod.GET, entity, String.class);
    }

    @Test
    void rejeitaRotaAutenticadaSemToken() {
        stubDownstream();

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/orders/x", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/api/v1/orders/x")));
    }

    @Test
    void repassaRotaAutenticadaComToken() {
        stubDownstream();

        ResponseEntity<String> response = authGet("/api/v1/orders/x",
                bearer(mintToken(UUID.randomUUID().toString(), "ROLE_USER")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"id\":\"x\"");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/api/v1/orders/x")));
    }

    @Test
    void rotaAdminNegaTokenNaoAdmin() {
        stubDownstream();

        ResponseEntity<String> response = authGet("/api/v1/audit-events",
                bearer(mintToken(UUID.randomUUID().toString(), "ROLE_USER")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rotaAdminAceitaTokenAdmin() {
        stubDownstream();

        ResponseEntity<String> response = authGet("/api/v1/audit-events",
                bearer(mintToken(UUID.randomUUID().toString(), "ROLE_ADMIN")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void rotaDeAuthEhPublica() {
        stubDownstream();

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/auth/login", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("accessToken");
    }

    @Test
    void agregacaoDeOpenApiEhPublicaEReescreveOPath() {
        stubDownstream();

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/aggregate/order-service/v3/api-docs", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("openapi");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/v3/api-docs")));
    }

    @Test
    void swaggerUiEApiDocsSaoPublicos() {
        assertThat(restTemplate.getForEntity("/swagger-ui/index.html", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.getForEntity("/v3/api-docs/swagger-config", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void aplicaRateLimitPorSubject() {
        stubDownstream();
        HttpEntity<Void> token = bearer(mintToken("rate-limit-" + UUID.randomUUID(), "ROLE_USER"));

        boolean got429 = false;
        for (int i = 0; i < 20; i++) {
            if (authGet("/api/v1/orders/x", token).getStatusCode().value() == 429) {
                got429 = true;
                break;
            }
        }

        assertThat(got429).as("deveria receber 429 após estourar o limite").isTrue();
    }
}
