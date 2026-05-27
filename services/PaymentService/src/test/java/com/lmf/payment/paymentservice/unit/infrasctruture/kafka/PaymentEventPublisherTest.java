package com.lmf.payment.paymentservice.unit.infrasctruture.kafka;

import com.lmf.payment.paymentservice.infrastructure.kafka.outbox.PaymentEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private PaymentEventPublisher paymentEventPublisher;

    @BeforeEach
    void setUp() {

        paymentEventPublisher = new PaymentEventPublisher(kafkaTemplate);
    }

    @Test
    @DisplayName("Should publish event to Kafka topic")
    void shouldPublishEventToKafkaTopic() {

        String topic = "payment.created";
        String key = "payment-123";
        String payload = """
                {
                  "paymentId": "123",
                  "status": "APPROVED"
                }
                """;

        paymentEventPublisher.publish(topic, key, payload);

        verify(kafkaTemplate).send(topic, key, payload);
    }
}