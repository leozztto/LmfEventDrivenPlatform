package com.lmf.inventory.inventoryservice.domain.repository;

import com.lmf.inventory.inventoryservice.domain.model.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);
}
