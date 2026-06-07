package com.lmf.inventory.inventoryservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.inventory.inventoryservice.domain.event.ProductCreatedEvent;
import com.lmf.inventory.inventoryservice.domain.exception.EventSerializationException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import com.lmf.inventory.inventoryservice.domain.repository.OutboxEventRepository;
import com.lmf.inventory.inventoryservice.infrastructure.outbox.OutboxStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryEventServiceTest {

    private OutboxEventRepository outboxEventRepository;

    private ObjectMapper objectMapper;

    private InventoryEventService inventoryEventService;

    @BeforeEach
    void setUp() {

        outboxEventRepository = mock(OutboxEventRepository.class);

        objectMapper = mock(ObjectMapper.class);

        inventoryEventService = new InventoryEventService(outboxEventRepository, objectMapper);
    }

    @Test
    @DisplayName("Should create outbox event successfully")
    void shouldCreateOutboxEventSuccessfully() throws Exception {

        Product product = createProduct();

        when(objectMapper.writeValueAsString(any(ProductCreatedEvent.class))).thenReturn("{\"event\":\"product-created\"}");

        inventoryEventService.publish(product);

        ArgumentCaptor<OutboxEventEntity> outboxEventEntityArgumentCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(outboxEventRepository).save(outboxEventEntityArgumentCaptor.capture());

        OutboxEventEntity outboxEventEntity = outboxEventEntityArgumentCaptor.getValue();

        assertThat(outboxEventEntity.getAggregateId()).isEqualTo(product.getId());

        assertThat(outboxEventEntity.getAggregateType()).isEqualTo("PRODUCT");

        assertThat(outboxEventEntity.getEventType()).isEqualTo("PRODUCT_CREATED");

        assertThat(outboxEventEntity.getPayload()).isEqualTo("{\"event\":\"product-created\"}");

        assertThat(outboxEventEntity.getOutboxStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should serialize ProductCreatedEvent")
    void shouldSerializeProductCreatedEvent() throws Exception {

        Product product = createProduct();

        when(objectMapper.writeValueAsString(any(ProductCreatedEvent.class))).thenReturn("{}");

        inventoryEventService.publish(product);

        verify(objectMapper).writeValueAsString(any(ProductCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw EventSerializationException when serialization fails")
    void shouldThrowEventSerializationException() throws Exception {

        Product product = createProduct();

        when(objectMapper.writeValueAsString(any(ProductCreatedEvent.class))).thenThrow(new JsonProcessingException("serialization error") {
        });

        assertThatThrownBy(() -> inventoryEventService.publish(product)).isInstanceOf(EventSerializationException.class).hasMessageContaining("Failed to serialize event");

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should preserve original exception")
    void shouldPreserveOriginalException() throws Exception {

        Product product = createProduct();

        JsonProcessingException exception = new JsonProcessingException("jackson failure") {
        };

        when(objectMapper.writeValueAsString(any(ProductCreatedEvent.class))).thenThrow(exception);

        assertThatThrownBy(() -> inventoryEventService.publish(product)).isInstanceOf(EventSerializationException.class).hasCause(exception);
    }

    private Product createProduct() {

        return Product.restore(UUID.randomUUID(), "SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), 10, 0, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now());
    }
}