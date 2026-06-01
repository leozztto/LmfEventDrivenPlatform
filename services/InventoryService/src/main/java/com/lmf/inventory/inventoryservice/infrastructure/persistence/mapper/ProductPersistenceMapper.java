package com.lmf.inventory.inventoryservice.infrastructure.persistence.mapper;

import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.ProductEntity;

public interface ProductPersistenceMapper {

    ProductEntity toEntity(Product product);

    Product toDomain(ProductEntity entity);
}
