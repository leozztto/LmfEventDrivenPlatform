package com.lmf.inventory.inventoryservice.infrastructure.persistence.mapper;

import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPersistenceMapperImplTest {

    private final ProductPersistenceMapperImpl mapper = new ProductPersistenceMapperImpl();

    @Test
    @DisplayName("Should convert domain to entity")
    void shouldConvertDomainToEntity() {

        Product product = Product.restore(UUID.randomUUID(), "SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), 10, 3, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now());

        ProductEntity entity = mapper.toEntity(product);

        assertThat(entity).isNotNull();

        assertThat(entity.getId()).isEqualTo(product.getId());

        assertThat(entity.getSku()).isEqualTo(product.getSku());

        assertThat(entity.getName()).isEqualTo(product.getName());

        assertThat(entity.getDescription()).isEqualTo(product.getDescription());

        assertThat(entity.getPrice()).isEqualTo(product.getPrice());

        assertThat(entity.getAvailableQuantity()).isEqualTo(product.getProductStock().getAvailableQuantity());

        assertThat(entity.getReservedQuantity()).isEqualTo(product.getProductStock().getReservedQuantity());

        assertThat(entity.getProductStatus()).isEqualTo(product.getProductStatus());

        assertThat(entity.getCreatedAt()).isEqualTo(product.getCreatedAt());

        assertThat(entity.getUpdatedAt()).isEqualTo(product.getUpdatedAt());
    }

    @Test
    @DisplayName("Should convert entity to domain")
    void shouldConvertEntityToDomain() {

        UUID id = UUID.randomUUID();

        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();

        ProductEntity entity = ProductEntity.builder().id(id).sku("SKU-001").name("Notebook").description("Notebook Gamer").price(BigDecimal.valueOf(5000)).availableQuantity(10).reservedQuantity(3).productStatus(ProductStatus.ACTIVE).createdAt(createdAt).updatedAt(updatedAt).build();

        Product product = mapper.toDomain(entity);

        assertThat(product).isNotNull();

        assertThat(product.getId()).isEqualTo(id);

        assertThat(product.getSku()).isEqualTo("SKU-001");

        assertThat(product.getName()).isEqualTo("Notebook");

        assertThat(product.getDescription()).isEqualTo("Notebook Gamer");

        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(5000));

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(10);

        assertThat(product.getProductStock().getReservedQuantity()).isEqualTo(3);

        assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);

        assertThat(product.getCreatedAt()).isEqualTo(createdAt);

        assertThat(product.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should preserve all values in round trip conversion")
    void shouldPreserveAllValuesInRoundTripConversion() {

        Product original = Product.restore(UUID.randomUUID(), "SKU-999", "Mouse", "Mouse Gamer", BigDecimal.valueOf(299.90), 50, 5, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(2), OffsetDateTime.now());

        ProductEntity entity = mapper.toEntity(original);

        Product restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());

        assertThat(restored.getSku()).isEqualTo(original.getSku());

        assertThat(restored.getName()).isEqualTo(original.getName());

        assertThat(restored.getDescription()).isEqualTo(original.getDescription());

        assertThat(restored.getPrice()).isEqualByComparingTo(original.getPrice());

        assertThat(restored.getProductStock().getAvailableQuantity()).isEqualTo(original.getProductStock().getAvailableQuantity());

        assertThat(restored.getProductStock().getReservedQuantity()).isEqualTo(original.getProductStock().getReservedQuantity());

        assertThat(restored.getProductStatus()).isEqualTo(original.getProductStatus());

        assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());

        assertThat(restored.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }
}