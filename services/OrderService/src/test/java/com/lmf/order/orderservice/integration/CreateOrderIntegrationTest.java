package com.lmf.order.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.order.orderservice.domain.model.outbox.OutboxStatus;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.order.orderservice.infrastructure.persistence.repository.SpringDataIdempotencyRepository;
import com.lmf.order.orderservice.infrastructure.persistence.repository.SpringDataOrderRepository;
import com.lmf.order.orderservice.infrastructure.web.request.CreateOrderRequest;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CreateOrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataOrderRepository springDataOrderRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private SpringDataIdempotencyRepository springDataIdempotencyRepository;

    @BeforeEach
    void setup() {
        springDataOrderRepository.deleteAll();
        springDataIdempotencyRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create order and outbox event successfully")
    void shouldCreateOrderAndOutboxEventSuccessfully() throws Exception {

        CreateOrderRequest createOrderRequest = TestDataFactory.createRequest();

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "idem-123").content(objectMapper.writeValueAsString(createOrderRequest))).andExpect(status().isCreated());

        assertFalse(springDataOrderRepository.findAll().isEmpty());

        List<OutboxEventEntity> events = outboxEventRepository.findAll();

        assertFalse(events.isEmpty());

        OutboxEventEntity outboxEventEntity = events.get(0);

        assertEquals("ORDER_CREATED", outboxEventEntity.getEventType());

        assertEquals(OutboxStatus.PUBLISHED, outboxEventEntity.getOutboxStatus());
    }

    @Test
    @DisplayName("Should not create duplicated order with same idempotency key")
    void shouldNotCreateDuplicatedOrderWithSameIdempotencyKey() throws Exception {

        String idempotencyKey = "testIntegration";

        CreateOrderRequest createOrderRequest = TestDataFactory.createRequest();

        String payload = objectMapper.writeValueAsString(createOrderRequest);

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", idempotencyKey).content(payload)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", idempotencyKey).content(payload)).andExpect(status().isCreated());

        assertEquals(1, springDataOrderRepository.count());
    }

    @Test
    @DisplayName("Should return bad request when payload is invalid")
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {

        String invalidPayload = """
                {
                  "items": []
                }
                """;

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "invalid-key").content(invalidPayload)).andExpect(status().isBadRequest());
    }
}
