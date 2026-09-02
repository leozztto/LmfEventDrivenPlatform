package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.domain.event.ProductCreatedEvent;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.platform.messaging.OutboxWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryEventService {

    static final String EVENT_TYPE_PRODUCT_CREATED = "PRODUCT_CREATED";

    private final OutboxWriter outboxWriter;

    public void publish(Product product) {

        ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.of(product);

        outboxWriter.write(productCreatedEvent.productId(), "PRODUCT", EVENT_TYPE_PRODUCT_CREATED, productCreatedEvent);
    }
}
