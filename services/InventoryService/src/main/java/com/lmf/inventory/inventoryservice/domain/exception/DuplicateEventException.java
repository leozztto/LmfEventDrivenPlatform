package com.lmf.inventory.inventoryservice.domain.exception;

public class DuplicateEventException extends RuntimeException {

    public DuplicateEventException(String eventId) {

        super("Event already processed: " + eventId);
    }
}
