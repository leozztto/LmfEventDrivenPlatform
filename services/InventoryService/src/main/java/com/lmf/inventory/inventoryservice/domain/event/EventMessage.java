package com.lmf.inventory.inventoryservice.domain.event;

import java.util.UUID;

public interface EventMessage {

    UUID eventId();

    String eventType();
}
