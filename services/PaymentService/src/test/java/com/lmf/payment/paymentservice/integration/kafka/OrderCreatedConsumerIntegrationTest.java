package com.lmf.payment.paymentservice.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.payment.paymentservice.events.OrderCreatedEvent;
import com.lmf.payment.paymentservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

class OrderCreatedConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldConsumeOrderCreatedEvent() throws Exception {

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(UUID.randomUUID(), "ORDER_CREATED", "1.0", OffsetDateTime.now(), UUID.randomUUID(), "CREATED", new BigDecimal("299.90"), null, null, null, List.of());

        kafkaTemplate.send("order.created", objectMapper.writeValueAsString(orderCreatedEvent));

        Thread.sleep(5000);
    }
}
