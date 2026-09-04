package com.lmf.platform.contracts;

import java.util.UUID;

/**
 * Envelope mínimo comum a todos os eventos de integração da plataforma. O {@code eventId} é a chave
 * de deduplicação usada pelo Inbox Pattern dos consumidores.
 */
public interface EventMessage {

    UUID eventId();

    String eventType();
}
