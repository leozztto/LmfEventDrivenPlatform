package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.domain.model.StockMovement;
import com.lmf.inventory.inventoryservice.domain.repository.StockMovementRepository;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.StockMovementEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockMovementRepositoryImpl implements StockMovementRepository {

    private final SpringDataStockMovementRepository springDataStockMovementRepository;

    @Override
    public void save(StockMovement stockMovement) {

        springDataStockMovementRepository.save(StockMovementEntity.builder()
                .id(stockMovement.getId())
                .productId(stockMovement.getProductId())
                .movementType(stockMovement.getType())
                .reason(stockMovement.getReason())
                .quantity(stockMovement.getQuantity())
                .availableAfter(stockMovement.getAvailableAfter())
                .reservedAfter(stockMovement.getReservedAfter())
                .createdAt(stockMovement.getCreatedAt())
                .build());
    }

    @Override
    public List<StockMovement> findByProductId(UUID productId) {

        return springDataStockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(e -> StockMovement.restore(e.getId(), e.getProductId(), e.getMovementType(), e.getReason(),
                        e.getQuantity(), e.getAvailableAfter(), e.getReservedAfter(), e.getCreatedAt()))
                .toList();
    }
}
