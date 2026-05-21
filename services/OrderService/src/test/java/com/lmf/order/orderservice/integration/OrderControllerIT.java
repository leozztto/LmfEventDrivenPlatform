package com.lmf.order.orderservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateOrderSuccessfully() {

        Map<String, Object> customer = Map.of("customerId", UUID.randomUUID(), "name", "João Silva", "email", "joao.silva@email.com", "phone", "(11) 99999-9999");

        Map<String, Object> shippingAddress = Map.of("street", "Rua das Flores", "number", "123", "city", "São Paulo", "zipCode", "01234-567", "country", "Brasil");

        Map<String, Object> payment = Map.of("paymentMethod", "CREDIT_CARD", "installments", 3, "amount", new BigDecimal("200.00"));

        Map<String, Object> item1 = Map.of("productId", UUID.randomUUID(), "quantity", 2, "unitPrice", new BigDecimal("100.00"));

        Map<String, Object> requestBody = Map.of("customer", customer, "shippingAddress", shippingAddress, "payment", payment, "items", List.of(item1));


        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        var response = restTemplate.postForEntity("/api/v1/orders", requestEntity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
    }
}