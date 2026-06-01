package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findBySku(String sku);

    boolean existsBySku(String sku);
}
