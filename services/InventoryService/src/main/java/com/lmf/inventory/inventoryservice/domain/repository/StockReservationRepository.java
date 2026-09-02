package com.lmf.inventory.inventoryservice.domain.repository;

import com.lmf.inventory.inventoryservice.domain.model.StockReservation;

import java.util.List;
import java.util.UUID;

public interface StockReservationRepository {

    void save(StockReservation stockReservation);

    void update(StockReservation stockReservation);

    List<StockReservation> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
