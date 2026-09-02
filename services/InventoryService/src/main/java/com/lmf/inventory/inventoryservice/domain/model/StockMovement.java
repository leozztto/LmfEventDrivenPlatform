package com.lmf.inventory.inventoryservice.domain.model;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro imutável de uma movimentação de estoque de um produto (entrada/saída manual), com o
 * saldo resultante — dá auditoria e histórico às operações manuais.
 */
@Getter
public class StockMovement {

    private final UUID id;

    private final UUID productId;

    private final StockMovementType type;

    private final StockMovementReason reason;

    private final Integer quantity;

    private final Integer availableAfter;

    private final Integer reservedAfter;

    private final OffsetDateTime createdAt;

    private StockMovement(UUID id, UUID productId, StockMovementType type, StockMovementReason reason,
                          Integer quantity, Integer availableAfter, Integer reservedAfter, OffsetDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.type = type;
        this.reason = reason;
        this.quantity = quantity;
        this.availableAfter = availableAfter;
        this.reservedAfter = reservedAfter;
        this.createdAt = createdAt;
    }

    public static StockMovement record(Product product, StockMovementType type, StockMovementReason reason, Integer quantity) {

        return new StockMovement(
                UUID.randomUUID(),
                product.getId(),
                type,
                reason,
                quantity,
                product.getProductStock().getAvailableQuantity(),
                product.getProductStock().getReservedQuantity(),
                OffsetDateTime.now());
    }

    public static StockMovement restore(UUID id, UUID productId, StockMovementType type, StockMovementReason reason,
                                        Integer quantity, Integer availableAfter, Integer reservedAfter, OffsetDateTime createdAt) {
        return new StockMovement(id, productId, type, reason, quantity, availableAfter, reservedAfter, createdAt);
    }
}
