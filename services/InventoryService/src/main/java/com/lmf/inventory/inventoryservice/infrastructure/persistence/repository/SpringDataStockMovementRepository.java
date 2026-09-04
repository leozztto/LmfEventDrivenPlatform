package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataStockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {

    List<StockMovementEntity> findByProductIdOrderByCreatedAtDesc(UUID productId);
}
