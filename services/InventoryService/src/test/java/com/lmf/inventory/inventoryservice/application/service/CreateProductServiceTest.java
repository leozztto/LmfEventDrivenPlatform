package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.application.command.ProductCommand;
import com.lmf.inventory.inventoryservice.domain.exception.ProductAlreadyExistsException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateProductServiceTest {

    private ProductRepository productRepository;

    private InventoryEventService inventoryEventService;

    private CreateProductService createProductService;

    @BeforeEach
    void setUp() {

        productRepository = mock(ProductRepository.class);

        inventoryEventService = mock(InventoryEventService.class);

        createProductService = new CreateProductService(productRepository, inventoryEventService);
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {

        ProductCommand productCommand = new ProductCommand("SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), 10);

        when(productRepository.existsBySku("SKU-001")).thenReturn(false);

        Product product = createProductService.execute(productCommand);

        assertThat(product).isNotNull();

        assertThat(product.getSku()).isEqualTo("SKU-001");

        assertThat(product.getName()).isEqualTo("Notebook");

        assertThat(product.getDescription()).isEqualTo("Notebook Gamer");

        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(5000));

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(10);

        verify(productRepository).existsBySku("SKU-001");

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getSku()).isEqualTo("SKU-001");

        verify(inventoryEventService).publish(savedProduct);
    }

    @Test
    @DisplayName("Should publish same product returned by service")
    void shouldPublishSameProductReturnedByService() {

        ProductCommand productCommand = new ProductCommand("SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), 10);

        when(productRepository.existsBySku(anyString())).thenReturn(false);

        Product product = createProductService.execute(productCommand);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        verify(inventoryEventService).publish(productCaptor.capture());

        Product publishedProduct = productCaptor.getValue();

        assertThat(publishedProduct.getId()).isEqualTo(product.getId());

        assertThat(publishedProduct.getSku()).isEqualTo(product.getSku());
    }

    @Test
    @DisplayName("Should throw exception when sku already exists")
    void shouldThrowExceptionWhenSkuAlreadyExists() {

        ProductCommand productCommand = new ProductCommand("SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), 10);

        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> createProductService.execute(productCommand)).isInstanceOf(ProductAlreadyExistsException.class).hasMessageContaining("SKU-001");

        verify(productRepository).existsBySku("SKU-001");

        verify(productRepository, never()).save(any());

        verifyNoInteractions(inventoryEventService);
    }

    @Test
    @DisplayName("Should not publish event when save is not executed")
    void shouldNotPublishEventWhenProductAlreadyExists() {

        ProductCommand productCommand = new ProductCommand("SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), 10);

        when(productRepository.existsBySku(anyString())).thenReturn(true);

        assertThatThrownBy(() -> createProductService.execute(productCommand)).isInstanceOf(ProductAlreadyExistsException.class);

        verify(inventoryEventService, never()).publish(any());
    }
}