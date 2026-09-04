package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.domain.event.ProductCreatedEvent;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import com.lmf.platform.messaging.OutboxWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

class InventoryEventServiceTest {

    private OutboxWriter outboxWriter;

    private InventoryEventService inventoryEventService;

    @BeforeEach
    void setUp() {

        outboxWriter = mock(OutboxWriter.class);

        inventoryEventService = new InventoryEventService(outboxWriter);
    }

    @Test
    @DisplayName("Deve escrever PRODUCT_CREATED no outbox com aggregateType PRODUCT")
    void shouldWriteProductCreatedToOutbox() {

        Product product = Product.restore(UUID.randomUUID(), "SKU-001", "Notebook", "Gamer", BigDecimal.valueOf(5000), 10, 0, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now());

        inventoryEventService.publish(product);

        verify(outboxWriter).write(eq(product.getId()), eq("PRODUCT"), eq("PRODUCT_CREATED"), any(ProductCreatedEvent.class));
    }
}
