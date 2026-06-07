package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.ProductEntity;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.mapper.ProductPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductRepositoryImplTest {

    private SpringDataProductRepository springDataProductRepository;

    private ProductPersistenceMapper productPersistenceMapper;

    private ProductRepositoryImpl productRepository;

    @BeforeEach
    void setUp() {

        springDataProductRepository = mock(SpringDataProductRepository.class);

        productPersistenceMapper = mock(ProductPersistenceMapper.class);

        productRepository = new ProductRepositoryImpl(springDataProductRepository, productPersistenceMapper);
    }

    @Test
    @DisplayName("Should save product")
    void shouldSaveProduct() {

        Product product = mock(Product.class);

        ProductEntity productEntity = mock(ProductEntity.class);

        Product savedProduct = mock(Product.class);

        when(productPersistenceMapper.toEntity(product)).thenReturn(productEntity);

        when(springDataProductRepository.save(productEntity)).thenReturn(productEntity);

        when(productPersistenceMapper.toDomain(productEntity)).thenReturn(savedProduct);

        Product result = productRepository.save(product);

        assertThat(result).isSameAs(savedProduct);

        verify(productPersistenceMapper).toEntity(product);

        verify(springDataProductRepository).save(productEntity);

        verify(productPersistenceMapper).toDomain(productEntity);
    }

    @Test
    @DisplayName("Should find product by id")
    void shouldFindProductById() {

        UUID id = UUID.randomUUID();

        ProductEntity productEntity = mock(ProductEntity.class);

        Product product = mock(Product.class);

        when(springDataProductRepository.findById(id)).thenReturn(Optional.of(productEntity));

        when(productPersistenceMapper.toDomain(productEntity)).thenReturn(product);

        Optional<Product> result = productRepository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(product);

        verify(springDataProductRepository).findById(id);
    }

    @Test
    @DisplayName("Should return empty when product not found by id")
    void shouldReturnEmptyWhenProductNotFoundById() {

        UUID id = UUID.randomUUID();

        when(springDataProductRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Product> product = productRepository.findById(id);

        assertThat(product).isEmpty();
    }

    @Test
    @DisplayName("Should find product by sku")
    void shouldFindProductBySku() {

        String sku = "SKU-001";

        ProductEntity productEntity = mock(ProductEntity.class);

        Product product = mock(Product.class);

        when(springDataProductRepository.findBySku(sku)).thenReturn(Optional.of(productEntity));

        when(productPersistenceMapper.toDomain(productEntity)).thenReturn(product);

        Optional<Product> result = productRepository.findBySku(sku);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(product);

        verify(springDataProductRepository).findBySku(sku);
    }

    @Test
    @DisplayName("Should return empty when product not found by sku")
    void shouldReturnEmptyWhenProductNotFoundBySku() {

        when(springDataProductRepository.findBySku("SKU")).thenReturn(Optional.empty());

        Optional<Product> product = productRepository.findBySku("SKU");

        assertThat(product).isEmpty();
    }

    @Test
    @DisplayName("Should verify product exists by sku")
    void shouldVerifyProductExistsBySku() {

        when(springDataProductRepository.existsBySku("SKU-001")).thenReturn(true);

        boolean result = productRepository.existsBySku("SKU-001");

        assertThat(result).isTrue();

        verify(springDataProductRepository).existsBySku("SKU-001");
    }

    @Test
    @DisplayName("Should return false when sku does not exist")
    void shouldReturnFalseWhenSkuDoesNotExist() {

        when(springDataProductRepository.existsBySku("SKU-001")).thenReturn(false);

        boolean result = productRepository.existsBySku("SKU-001");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should update stock successfully")
    void shouldUpdateStockSuccessfully() {

        UUID productId = UUID.randomUUID();

        Product product = Product.restore(productId, "SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), 15, 5, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now());

        ProductEntity productEntity = mock(ProductEntity.class);

        Product updatedProduct = mock(Product.class);

        when(springDataProductRepository.findById(productId)).thenReturn(Optional.of(productEntity));

        when(productPersistenceMapper.toDomain(productEntity)).thenReturn(updatedProduct);

        Product result = productRepository.update(product);

        assertThat(result).isSameAs(updatedProduct);

        verify(productEntity).updateStock(eq(15), eq(5), any(OffsetDateTime.class));

        verify(productPersistenceMapper).toDomain(productEntity);
    }

    @Test
    @DisplayName("Should throw exception when product not found during update")
    void shouldThrowExceptionWhenProductNotFoundDuringUpdate() {

        UUID productId = UUID.randomUUID();

        Product product = Product.restore(productId, "SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), 10, 0, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now());

        when(springDataProductRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productRepository.update(product)).isInstanceOf(java.util.NoSuchElementException.class);

        verify(productPersistenceMapper, never()).toDomain(any());
    }
}