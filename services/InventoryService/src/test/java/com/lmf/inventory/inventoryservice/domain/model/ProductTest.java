package com.lmf.inventory.inventoryservice.domain.model;

import com.lmf.inventory.inventoryservice.domain.exception.InvalidProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {

        Product product = Product.create("SKU-001", "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), 10);

        assertThat(product).isNotNull();
        assertThat(product.getId()).isNotNull();

        assertThat(product.getSku()).isEqualTo("SKU-001");
        assertThat(product.getName()).isEqualTo("Notebook");
        assertThat(product.getDescription()).isEqualTo("Notebook Gamer");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(5000));

        assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(10);

        assertThat(product.getProductStock().getReservedQuantity()).isZero();

        assertThat(product.getCreatedAt()).isNotNull();
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when sku is null")
    void shouldThrowExceptionWhenSkuIsNull() {

        assertThatThrownBy(() -> Product.create(null, "Notebook", "Description", BigDecimal.TEN, 10)).isInstanceOf(InvalidProductException.class).hasMessage("SKU is required");
    }

    @Test
    @DisplayName("Should throw exception when sku is blank")
    void shouldThrowExceptionWhenSkuIsBlank() {

        assertThatThrownBy(() -> Product.create(" ", "Notebook", "Description", BigDecimal.TEN, 10)).isInstanceOf(InvalidProductException.class).hasMessage("SKU is required");
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowExceptionWhenNameIsNull() {

        assertThatThrownBy(() -> Product.create("SKU-001", null, "Description", BigDecimal.TEN, 10)).isInstanceOf(InvalidProductException.class).hasMessage("Name is required");
    }

    @Test
    @DisplayName("Should throw exception when name is blank")
    void shouldThrowExceptionWhenNameIsBlank() {

        assertThatThrownBy(() -> Product.create("SKU-001", " ", "Description", BigDecimal.TEN, 10)).isInstanceOf(InvalidProductException.class).hasMessage("Name is required");
    }

    @Test
    @DisplayName("Should throw exception when price is null")
    void shouldThrowExceptionWhenPriceIsNull() {

        assertThatThrownBy(() -> Product.create("SKU-001", "Notebook", "Description", null, 10)).isInstanceOf(InvalidProductException.class).hasMessage("Price must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when price is zero")
    void shouldThrowExceptionWhenPriceIsZero() {

        assertThatThrownBy(() -> Product.create("SKU-001", "Notebook", "Description", BigDecimal.ZERO, 10)).isInstanceOf(InvalidProductException.class).hasMessage("Price must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when price is negative")
    void shouldThrowExceptionWhenPriceIsNegative() {

        assertThatThrownBy(() -> Product.create("SKU-001", "Notebook", "Description", BigDecimal.valueOf(-1), 10)).isInstanceOf(InvalidProductException.class).hasMessage("Price must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when initial stock is null")
    void shouldThrowExceptionWhenInitialStockIsNull() {

        assertThatThrownBy(() -> Product.create("SKU-001", "Notebook", "Description", BigDecimal.TEN, null)).isInstanceOf(InvalidProductException.class).hasMessage("Initial stock cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception when initial stock is negative")
    void shouldThrowExceptionWhenInitialStockIsNegative() {

        assertThatThrownBy(() -> Product.create("SKU-001", "Notebook", "Description", BigDecimal.TEN, -1)).isInstanceOf(InvalidProductException.class).hasMessage("Initial stock cannot be negative");
    }

    @Test
    @DisplayName("Should restore product successfully")
    void shouldRestoreProductSuccessfully() {

        UUID id = UUID.randomUUID();

        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();

        Product product = Product.restore(id, "SKU-001", "Notebook", "Description", BigDecimal.valueOf(100), 10, 5, ProductStatus.ACTIVE, createdAt, updatedAt);

        assertThat(product.getId()).isEqualTo(id);
        assertThat(product.getSku()).isEqualTo("SKU-001");
        assertThat(product.getName()).isEqualTo("Notebook");

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(10);

        assertThat(product.getProductStock().getReservedQuantity()).isEqualTo(5);

        assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);

        assertThat(product.getCreatedAt()).isEqualTo(createdAt);
        assertThat(product.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should reserve stock")
    void shouldReserveStock() {

        Product product = Product.create("SKU-001", "Notebook", "Description", BigDecimal.TEN, 10);

        product.reserveStock(3);

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(7);

        assertThat(product.getProductStock().getReservedQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should release reserved stock")
    void shouldReleaseReservedStock() {

        Product product = Product.create("SKU-001", "Notebook", "Description", BigDecimal.TEN, 10);

        product.reserveStock(4);
        product.releaseStock(2);

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(8);

        assertThat(product.getProductStock().getReservedQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should confirm reserved stock")
    void shouldConfirmReservedStock() {

        Product product = Product.create("SKU-001", "Notebook", "Description", BigDecimal.TEN, 10);

        product.reserveStock(4);
        product.confirmReservedStock(4);

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(6);

        assertThat(product.getProductStock().getReservedQuantity()).isZero();
    }

    @Test
    @DisplayName("Should add stock")
    void shouldAddStock() {

        Product product = Product.create("SKU-001", "Notebook", "Description", BigDecimal.TEN, 10);

        product.addStock(5);

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should remove stock")
    void shouldRemoveStock() {

        Product product = Product.create("SKU-001", "Notebook", "Description", BigDecimal.TEN, 10);

        product.removeStock(3);

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("Should update updatedAt when stock operation occurs")
    void shouldUpdateUpdatedAtWhenStockOperationOccurs() {

        Product product = Product.create("SKU-001", "Notebook", "Description", BigDecimal.TEN, 10);

        OffsetDateTime oldUpdatedAt = product.getUpdatedAt();

        product.addStock(1);

        assertThat(product.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }
}