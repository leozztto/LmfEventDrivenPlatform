package com.lmf.inventory.inventoryservice.infrastructure.persistence.entity;

import com.lmf.inventory.inventoryservice.domain.model.StockMovementReason;
import com.lmf.inventory.inventoryservice.domain.model.StockMovementType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockMovementEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private StockMovementType movementType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementReason reason;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "available_after", nullable = false)
    private Integer availableAfter;

    @Column(name = "reserved_after", nullable = false)
    private Integer reservedAfter;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
