package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.InventoryReservedEvent;
import com.lmf.platform.messaging.OutboxWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class ReserveInventoryEventServiceTest {

    private OutboxWriter outboxWriter;

    private ReserveInventoryEventService reserveInventoryEventService;

    @BeforeEach
    void setUp() {

        outboxWriter = mock(OutboxWriter.class);

        reserveInventoryEventService = new ReserveInventoryEventService(outboxWriter);
    }

    @Test
    @DisplayName("Deve escrever INVENTORY_RESERVED no outbox com o pedido como aggregate")
    void shouldWriteReservedEvent() {

        InventoryReservedEvent event = new InventoryReservedEvent(UUID.randomUUID(), InventoryReservedEvent.TYPE, "v1",
                OffsetDateTime.now(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, null, List.of());

        reserveInventoryEventService.publishSuccess(event);

        verify(outboxWriter).write(event.orderId(), "ORDER", "INVENTORY_RESERVED", event);
    }

    @Test
    @DisplayName("Deve escrever INVENTORY_RESERVATION_FAILED no outbox")
    void shouldWriteFailedEvent() {

        InventoryReservationFailedEvent event = new InventoryReservationFailedEvent(UUID.randomUUID(),
                InventoryReservationFailedEvent.TYPE, "v1", OffsetDateTime.now(), UUID.randomUUID(), "Insufficient stock");

        reserveInventoryEventService.publishFailure(event);

        verify(outboxWriter).write(event.orderId(), "ORDER", "INVENTORY_RESERVATION_FAILED", event);
    }
}
