package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.application.command.StockMovementCommand;
import com.lmf.inventory.inventoryservice.domain.exception.ProductNotFoundException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import com.lmf.inventory.inventoryservice.domain.model.StockMovementReason;
import com.lmf.inventory.inventoryservice.domain.model.StockMovementType;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockMovementServiceTest {

    private ProductRepository productRepository;

    private StockMovementService stockMovementService;

    @BeforeEach
    void setUp() {

        productRepository = mock(ProductRepository.class);

        stockMovementService = new StockMovementService(productRepository);
    }

    @Test
    @DisplayName("Should add stock when movement type is IN")
    void shouldAddStockWhenMovementTypeIsIn() {

        UUID productId = UUID.randomUUID();

        Product product = createProduct(productId, 10);

        StockMovementCommand stockMovementCommand = new StockMovementCommand(productId, StockMovementType.IN, 5, StockMovementReason.PURCHASE);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        when(productRepository.update(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = stockMovementService.execute(stockMovementCommand);

        assertThat(result).isNotNull();

        assertThat(result.getProductStock().getAvailableQuantity()).isEqualTo(15);

        assertThat(result.getProductStock().getReservedQuantity()).isZero();

        verify(productRepository).findById(productId);

        verify(productRepository).update(argThat(updatedProduct -> updatedProduct.getProductStock().getAvailableQuantity() == 15));
    }

    @Test
    @DisplayName("Should remove stock when movement type is OUT")
    void shouldRemoveStockWhenMovementTypeIsOut() {

        UUID productId = UUID.randomUUID();

        Product product = createProduct(productId, 20);

        StockMovementCommand stockMovementCommand = new StockMovementCommand(productId, StockMovementType.OUT, 7, StockMovementReason.MANUAL);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        when(productRepository.update(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = stockMovementService.execute(stockMovementCommand);

        assertThat(result).isNotNull();

        assertThat(result.getProductStock().getAvailableQuantity()).isEqualTo(13);

        assertThat(result.getProductStock().getReservedQuantity()).isZero();

        verify(productRepository).findById(productId);

        verify(productRepository).update(argThat(updatedProduct -> updatedProduct.getProductStock().getAvailableQuantity() == 13));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product does not exist")
    void shouldThrowProductNotFoundException() {

        UUID productId = UUID.randomUUID();

        StockMovementCommand stockMovementCommand = new StockMovementCommand(productId, StockMovementType.IN, 5, StockMovementReason.PURCHASE);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMovementService.execute(stockMovementCommand)).isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(productId);

        verify(productRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should propagate exception when removing more stock than available")
    void shouldPropagateExceptionWhenRemovingMoreStockThanAvailable() {

        UUID productId = UUID.randomUUID();

        Product product = createProduct(productId, 5);

        StockMovementCommand stockMovementCommand = new StockMovementCommand(productId, StockMovementType.OUT, 10, StockMovementReason.MANUAL);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> stockMovementService.execute(stockMovementCommand)).isInstanceOf(RuntimeException.class);

        verify(productRepository).findById(productId);

        verify(productRepository, never()).update(any());
    }

    private Product createProduct(UUID productId, Integer stock) {

        return Product.restore(productId, "SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), stock, 0, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now());
    }
}