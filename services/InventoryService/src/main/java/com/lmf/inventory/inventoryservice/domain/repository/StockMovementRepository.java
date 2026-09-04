package com.lmf.inventory.inventoryservice.domain.repository;

import com.lmf.inventory.inventoryservice.domain.model.StockMovement;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository {

    void save(StockMovement stockMovement);

    List<StockMovement> findByProductId(UUID productId);
}
