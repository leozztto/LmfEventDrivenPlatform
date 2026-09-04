package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import com.lmf.inventory.inventoryservice.domain.model.StockReservation;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import com.lmf.inventory.inventoryservice.domain.repository.StockReservationRepository;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.InventoryReservedEvent;
import com.lmf.platform.contracts.OrderItem;
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

    private StockReservationRepository stockReservationRepository;

    private ReserveInventoryEventService reserveInventoryEventService;

    private ReserveInventoryService reserveInventoryService;

    @BeforeEach
    void setUp() {

        productRepository = mock(ProductRepository.class);

        stockReservationRepository = mock(StockReservationRepository.class);

        reserveInventoryEventService = mock(ReserveInventoryEventService.class);

        reserveInventoryService = new ReserveInventoryService(productRepository, stockReservationRepository, reserveInventoryEventService);
    }

    @Test
    @DisplayName("Should reserve inventory and publish a single INVENTORY_RESERVED event")
    void shouldReserveInventorySuccessfully() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = createProduct(productId, 10);

        FraudApprovedEvent fraudApprovedEvent = createFraudApprovedEvent(orderId, productId, 3);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        reserveInventoryService.execute(fraudApprovedEvent);

        verify(productRepository).update(product);
        verify(productRepository, never()).save(any());
        verify(stockReservationRepository).save(any(StockReservation.class));

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(7);
        assertThat(product.getProductStock().getReservedQuantity()).isEqualTo(3);

        ArgumentCaptor<InventoryReservedEvent> captor = ArgumentCaptor.forClass(InventoryReservedEvent.class);

        verify(reserveInventoryEventService).publishSuccess(captor.capture());
        verify(reserveInventoryEventService, never()).publishFailure(any());

        InventoryReservedEvent reserved = captor.getValue();

        assertThat(reserved.orderId()).isEqualTo(orderId);
        assertThat(reserved.eventType()).isEqualTo("INVENTORY_RESERVED");
        assertThat(reserved.items()).hasSize(1);
        assertThat(reserved.items().get(0).productId()).isEqualTo(productId);
        assertThat(reserved.items().get(0).quantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should publish a failure event and not rethrow when the product does not exist")
    void shouldPublishFailureEventWhenProductNotFound() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        FraudApprovedEvent fraudApprovedEvent = createFraudApprovedEvent(orderId, productId, 2);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatCode(() -> reserveInventoryService.execute(fraudApprovedEvent)).doesNotThrowAnyException();

        verify(productRepository, never()).update(any());
        verify(productRepository, never()).save(any());
        verify(stockReservationRepository, never()).save(any());
        verify(reserveInventoryEventService, never()).publishSuccess(any());

        ArgumentCaptor<InventoryReservationFailedEvent> captor = ArgumentCaptor.forClass(InventoryReservationFailedEvent.class);

        verify(reserveInventoryEventService).publishFailure(captor.capture());

        InventoryReservationFailedEvent failed = captor.getValue();

        assertThat(failed.orderId()).isEqualTo(orderId);
        assertThat(failed.eventType()).isEqualTo("INVENTORY_RESERVATION_FAILED");
        assertThat(failed.reason()).contains(productId.toString());
    }

    @Test
    @DisplayName("Should publish a failure event and not persist any reservation when stock is insufficient")
    void shouldPublishFailureEventWhenStockReservationFails() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = createProduct(productId, 1);

        FraudApprovedEvent fraudApprovedEvent = createFraudApprovedEvent(orderId, productId, 10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatCode(() -> reserveInventoryService.execute(fraudApprovedEvent)).doesNotThrowAnyException();

        verify(productRepository, never()).update(any());
        verify(productRepository, never()).save(any());
        verify(reserveInventoryEventService).publishFailure(any(InventoryReservationFailedEvent.class));
        verify(reserveInventoryEventService, never()).publishSuccess(any());
    }

    @Test
    @DisplayName("Should reserve all items and publish one event carrying every reserved item")
    void shouldProcessMultipleOrderItems() {

        UUID orderId = UUID.randomUUID();

        UUID product1 = UUID.randomUUID();
        UUID product2 = UUID.randomUUID();

        Product firstProduct = createProduct(product1, 10);
        Product secondProduct = createProduct(product2, 20);

        FraudApprovedEvent fraudApprovedEvent = createFraudApprovedEventWithTwoItems(orderId, product1, product2);

        when(productRepository.findById(product1)).thenReturn(Optional.of(firstProduct));
        when(productRepository.findById(product2)).thenReturn(Optional.of(secondProduct));

        reserveInventoryService.execute(fraudApprovedEvent);

        verify(productRepository, times(2)).update(any(Product.class));
        verify(stockReservationRepository, times(2)).save(any(StockReservation.class));

        ArgumentCaptor<InventoryReservedEvent> captor = ArgumentCaptor.forClass(InventoryReservedEvent.class);
        verify(reserveInventoryEventService).publishSuccess(captor.capture());

        assertThat(captor.getValue().items()).hasSize(2);
        assertThat(firstProduct.getProductStock().getReservedQuantity()).isEqualTo(2);
        assertThat(secondProduct.getProductStock().getReservedQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should not persist the first item when a later item fails")
    void shouldNotPersistPartialReservation() {

        UUID orderId = UUID.randomUUID();

        UUID product1 = UUID.randomUUID();
        UUID product2 = UUID.randomUUID();

        Product firstProduct = createProduct(product1, 10);
        Product secondProduct = createProduct(product2, 1);

        FraudApprovedEvent fraudApprovedEvent = fraudEvent(orderId, item(product1, 2), item(product2, 50));

        when(productRepository.findById(product1)).thenReturn(Optional.of(firstProduct));
        when(productRepository.findById(product2)).thenReturn(Optional.of(secondProduct));

        reserveInventoryService.execute(fraudApprovedEvent);

        verify(productRepository, never()).update(any());
        verify(reserveInventoryEventService).publishFailure(any(InventoryReservationFailedEvent.class));
    }

    private Product createProduct(UUID id, int stock) {

        return Product.restore(id, "SKU-" + id, "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), stock, 0, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now());
    }

    private FraudApprovedEvent createFraudApprovedEvent(UUID orderId, UUID productId, Integer quantity) {

        return fraudEvent(orderId, item(productId, quantity));
    }

    private FraudApprovedEvent createFraudApprovedEventWithTwoItems(UUID orderId, UUID product1, UUID product2) {

        return fraudEvent(orderId, item(product1, 2), item(product2, 5));
    }

    private OrderItem item(UUID productId, int quantity) {

        return new OrderItem(productId, quantity, BigDecimal.valueOf(10), BigDecimal.valueOf(10L * quantity));
    }

    private FraudApprovedEvent fraudEvent(UUID orderId, OrderItem... items) {

        return new FraudApprovedEvent(UUID.randomUUID(), FraudApprovedEvent.TYPE, "v1", OffsetDateTime.now(), orderId, null, BigDecimal.TEN, null, List.of(items));
    }
}
