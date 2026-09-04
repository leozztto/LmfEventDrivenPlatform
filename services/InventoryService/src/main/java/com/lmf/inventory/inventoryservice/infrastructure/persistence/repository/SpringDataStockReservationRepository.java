package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.StockReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataStockReservationRepository extends JpaRepository<StockReservationEntity, UUID> {

    List<StockReservationEntity> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
