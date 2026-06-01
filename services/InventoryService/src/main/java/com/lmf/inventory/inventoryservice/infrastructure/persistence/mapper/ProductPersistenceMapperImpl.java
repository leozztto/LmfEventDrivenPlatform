package com.lmf.inventory.inventoryservice.infrastructure.persistence.mapper;

import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductPersistenceMapperImpl implements ProductPersistenceMapper {

    @Override
    public ProductEntity toEntity(Product product) {

        return ProductEntity.builder().id(product.getId()).sku(product.getSku()).name(product.getName()).description(product.getDescription()).price(product.getPrice()).availableQuantity(product.getProductStock().getAvailableQuantity()).reservedQuantity(product.getProductStock().getReservedQuantity()).productStatus(product.getProductStatus()).createdAt(product.getCreatedAt()).updatedAt(product.getUpdatedAt()).build();
    }

    @Override
    public Product toDomain(ProductEntity productEntity) {

        return Product.restore(productEntity.getId(), productEntity.getSku(), productEntity.getName(), productEntity.getDescription(), productEntity.getPrice(), productEntity.getAvailableQuantity(), productEntity.getReservedQuantity(), productEntity.getProductStatus(), productEntity.getCreatedAt(), productEntity.getUpdatedAt());
    }
}
