package com.lmf.platform.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Publica no Kafka de forma síncrona — aguarda a confirmação do broker antes de retornar, para que o
 * relay só marque o evento como {@code PUBLISHED} quando a entrega for confirmada (corrige o
 * fire-and-forget que existia em cada serviço).
 */
@RequiredArgsConstructor
public class MessagePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topic, String key, String payload) {

        try {

            kafkaTemplate.send(topic, key, payload).get(10, TimeUnit.SECONDS);

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();
            throw new MessagePublishException("Interrupted while publishing to " + topic, ex);

        } catch (Exception ex) {

            throw new MessagePublishException("Failed to publish to " + topic, ex);
        }
    }

    public static class MessagePublishException extends RuntimeException {
        public MessagePublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
