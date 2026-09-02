package com.lmf.platform.messaging;

/**
 * Lançada quando um evento já registrado no inbox é registrado de novo (corrida entre consumidores).
 * Tratada como no-op idempotente pelo {@link AbstractInboxConsumer}.
 */
public class DuplicateEventException extends RuntimeException {

    public DuplicateEventException(String eventId) {
        super("Event already registered in inbox: " + eventId);
    }
}
