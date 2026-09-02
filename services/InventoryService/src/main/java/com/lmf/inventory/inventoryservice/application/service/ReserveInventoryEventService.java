package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.InventoryReservedEvent;
import com.lmf.platform.messaging.OutboxWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveInventoryEventService {

    private final OutboxWriter outboxWriter;

    public void publishSuccess(InventoryReservedEvent event) {

        outboxWriter.write(event.orderId(), "ORDER", InventoryReservedEvent.TYPE, event);
    }

    public void publishFailure(InventoryReservationFailedEvent event) {

        outboxWriter.write(event.orderId(), "ORDER", InventoryReservationFailedEvent.TYPE, event);
    }
}
