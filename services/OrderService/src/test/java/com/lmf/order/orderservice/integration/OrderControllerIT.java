package com.lmf.order.orderservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateOrderSuccessfully() {

        Map<String, Object> requestBody = Map.of("customerId", UUID.randomUUID(), "items", java.util.List.of(Map.of("productId", UUID.randomUUID(), "quantity", 2, "unitPrice", 100)));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        var response = restTemplate.postForEntity("/api/v1/orders", requestEntity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
    }
}