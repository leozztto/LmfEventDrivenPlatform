package com.lmf.inventory.inventoryservice.domain.model;

import com.lmf.inventory.inventoryservice.domain.exception.InvalidStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class StockReservationTest {

    @Test
    @DisplayName("Deve criar reserva no estado RESERVED")
    void shouldCreateReservationAsReserved() {

        StockReservation reservation = StockReservation.create(UUID.randomUUID(), UUID.randomUUID(), 5);

        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getQuantity()).isEqualTo(5);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.isPending()).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar quantidade inválida")
    void shouldRejectInvalidQuantity() {

        assertThatThrownBy(() -> StockReservation.create(UUID.randomUUID(), UUID.randomUUID(), 0))
                .isInstanceOf(InvalidStockException.class);
    }

    @Test
    @DisplayName("Deve transicionar para RELEASED e CONFIRMED")
    void shouldTransitionStatus() {

        StockReservation released = StockReservation.create(UUID.randomUUID(), UUID.randomUUID(), 1);
        released.markReleased();
        assertThat(released.getStatus()).isEqualTo(ReservationStatus.RELEASED);
        assertThat(released.isPending()).isFalse();

        StockReservation confirmed = StockReservation.create(UUID.randomUUID(), UUID.randomUUID(), 1);
        confirmed.markConfirmed();
        assertThat(confirmed.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
