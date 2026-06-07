package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.domain.event.InventoryReservationFailedEvent;
import com.lmf.inventory.inventoryservice.domain.event.InventoryReservationSuccessEvent;
import com.lmf.inventory.inventoryservice.domain.event.OrderCreatedEvent;
import com.lmf.inventory.inventoryservice.domain.event.order.OrderItem;
import com.lmf.inventory.inventoryservice.domain.exception.ProductNotFoundException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReserveInventoryServiceTest {

    private ProductRepository productRepository;

    private ReserveInventoryEventService reserveInventoryEventService;

    private ReserveInventoryService reserveInventoryService;

    @BeforeEach
    void setUp() {

        productRepository = mock(ProductRepository.class);

        reserveInventoryEventService = mock(ReserveInventoryEventService.class);

        reserveInventoryService = new ReserveInventoryService(productRepository, reserveInventoryEventService);
    }

    @Test
    @DisplayName("Should reserve inventory successfully")
    void shouldReserveInventorySuccessfully() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = createProduct(productId, 10);

        OrderCreatedEvent orderCreatedEvent = createOrderCreatedEvent(orderId, productId, 3);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        reserveInventoryService.execute(orderCreatedEvent);

        verify(productRepository).save(product);

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(7);

        assertThat(product.getProductStock().getReservedQuantity()).isEqualTo(3);

        ArgumentCaptor<InventoryReservationSuccessEvent> captor = ArgumentCaptor.forClass(InventoryReservationSuccessEvent.class);

        verify(reserveInventoryEventService).publishSuccess(captor.capture());

        InventoryReservationSuccessEvent success = captor.getValue();

        assertThat(success.orderId()).isEqualTo(orderId);

        assertThat(success.productId()).isEqualTo(productId);

        assertThat(success.eventType()).isEqualTo("INVENTORY_RESERVED");
    }

    @Test
    @DisplayName("Should publish failure event when product not found")
    void shouldPublishFailureEventWhenProductNotFound() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderCreatedEvent orderCreatedEvent = createOrderCreatedEvent(orderId, productId, 2);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveInventoryService.execute(orderCreatedEvent)).isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any());

        ArgumentCaptor<InventoryReservationFailedEvent> captor = ArgumentCaptor.forClass(InventoryReservationFailedEvent.class);

        verify(reserveInventoryEventService).publishFailure(captor.capture());

        InventoryReservationFailedEvent failed = captor.getValue();

        assertThat(failed.orderId()).isEqualTo(orderId);

        assertThat(failed.productId()).isEqualTo(productId);

        assertThat(failed.reason()).contains(productId.toString());
    }

    @Test
    @DisplayName("Should publish failure event when stock reservation fails")
    void shouldPublishFailureEventWhenStockReservationFails() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = createProduct(productId, 1);

        OrderCreatedEvent orderCreatedEvent = createOrderCreatedEvent(orderId, productId, 10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> reserveInventoryService.execute(orderCreatedEvent)).isInstanceOf(RuntimeException.class);

        verify(productRepository, never()).save(any());

        verify(reserveInventoryEventService).publishFailure(any(InventoryReservationFailedEvent.class));
    }

    @Test
    @DisplayName("Should process multiple order items")
    void shouldProcessMultipleOrderItems() {

        UUID orderId = UUID.randomUUID();

        UUID product1 = UUID.randomUUID();
        UUID product2 = UUID.randomUUID();

        Product firstProduct = createProduct(product1, 10);

        Product secondProduct = createProduct(product2, 20);

        OrderCreatedEvent orderCreatedEvent = createOrderCreatedEventWithTwoItems(orderId, product1, product2);

        when(productRepository.findById(product1)).thenReturn(Optional.of(firstProduct));

        when(productRepository.findById(product2)).thenReturn(Optional.of(secondProduct));

        reserveInventoryService.execute(orderCreatedEvent);

        verify(productRepository, times(2)).save(any(Product.class));

        verify(reserveInventoryEventService, times(2)).publishSuccess(any(InventoryReservationSuccessEvent.class));

        assertThat(firstProduct.getProductStock().getReservedQuantity()).isEqualTo(2);

        assertThat(secondProduct.getProductStock().getReservedQuantity()).isEqualTo(5);
    }

    private Product createProduct(UUID id, int stock) {

        return Product.restore(id, "SKU-" + id, "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), stock, 0, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now());
    }

    private OrderCreatedEvent createOrderCreatedEvent(UUID orderId, UUID productId, Integer quantity) {

        OrderItem item = mock(OrderItem.class);

        when(item.getProductId()).thenReturn(productId);
        when(item.getQuantity()).thenReturn(quantity);

        return new OrderCreatedEvent(UUID.randomUUID(), "ORDER_CREATED", "1.0", OffsetDateTime.now(), orderId, "CREATED", BigDecimal.TEN, null, null, null, List.of(item));
    }

    private OrderCreatedEvent createOrderCreatedEventWithTwoItems(UUID orderId, UUID product1, UUID product2) {

        OrderItem item1 = mock(OrderItem.class);
        when(item1.getProductId()).thenReturn(product1);
        when(item1.getQuantity()).thenReturn(2);

        OrderItem item2 = mock(OrderItem.class);
        when(item2.getProductId()).thenReturn(product2);
        when(item2.getQuantity()).thenReturn(5);

        return new OrderCreatedEvent(UUID.randomUUID(), "ORDER_CREATED", "1.0", OffsetDateTime.now(), orderId, "CREATED", BigDecimal.TEN, null, null, null, List.of(item1, item2));
    }
}