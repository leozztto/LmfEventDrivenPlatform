package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.domain.model.StockReservation;
import com.lmf.inventory.inventoryservice.domain.repository.StockReservationRepository;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.StockReservationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockReservationRepositoryImpl implements StockReservationRepository {

    private final SpringDataStockReservationRepository springDataStockReservationRepository;

    @Override
    public void save(StockReservation stockReservation) {

        springDataStockReservationRepository.save(toEntity(stockReservation));
    }

    @Override
    public void update(StockReservation stockReservation) {

        StockReservationEntity entity = springDataStockReservationRepository.findById(stockReservation.getId()).orElseThrow();

        entity.updateStatus(stockReservation.getStatus(), OffsetDateTime.now());
    }

    @Override
    public List<StockReservation> findByOrderId(UUID orderId) {

        return springDataStockReservationRepository.findByOrderId(orderId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {

        return springDataStockReservationRepository.existsByOrderId(orderId);
    }

    private StockReservationEntity toEntity(StockReservation reservation) {

        return StockReservationEntity.builder()
                .id(reservation.getId())
                .orderId(reservation.getOrderId())
                .productId(reservation.getProductId())
                .quantity(reservation.getQuantity())
                .reservationStatus(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    private StockReservation toDomain(StockReservationEntity entity) {

        return StockReservation.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getReservationStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
