package com.lmf.inventory.inventoryservice.domain.model;

import com.lmf.inventory.inventoryservice.domain.exception.InvalidStockException;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registra o estoque reservado de um produto para um pedido, permitindo compensar (liberar) ou
 * confirmar a reserva quando o desfecho do pagamento chega — sem depender de reprocessar o evento
 * original de reserva.
 */
@Getter
public class StockReservation {

    private UUID id;

    private UUID orderId;

    private UUID productId;

    private Integer quantity;

    private ReservationStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public static StockReservation create(UUID orderId, UUID productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new InvalidStockException("Reservation quantity must be greater than zero");
        }

        StockReservation reservation = new StockReservation();

        reservation.id = UUID.randomUUID();
        reservation.orderId = orderId;
        reservation.productId = productId;
        reservation.quantity = quantity;
        reservation.status = ReservationStatus.RESERVED;
        reservation.createdAt = OffsetDateTime.now();
        reservation.updatedAt = reservation.createdAt;

        return reservation;
    }

    public static StockReservation restore(UUID id, UUID orderId, UUID productId, Integer quantity, ReservationStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {

        StockReservation reservation = new StockReservation();

        reservation.id = id;
        reservation.orderId = orderId;
        reservation.productId = productId;
        reservation.quantity = quantity;
        reservation.status = status;
        reservation.createdAt = createdAt;
        reservation.updatedAt = updatedAt;

        return reservation;
    }

    public boolean isPending() {
        return status == ReservationStatus.RESERVED;
    }

    public void markReleased() {
        this.status = ReservationStatus.RELEASED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markConfirmed() {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = OffsetDateTime.now();
    }
}
