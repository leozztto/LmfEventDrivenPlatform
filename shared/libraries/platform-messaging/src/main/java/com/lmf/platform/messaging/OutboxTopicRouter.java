package com.lmf.platform.messaging;

/**
 * Cada serviço fornece um bean que mapeia o {@code eventType} da linha do outbox para o tópico Kafka
 * de destino.
 */
@FunctionalInterface
public interface OutboxTopicRouter {

    String topicFor(String eventType);
}
