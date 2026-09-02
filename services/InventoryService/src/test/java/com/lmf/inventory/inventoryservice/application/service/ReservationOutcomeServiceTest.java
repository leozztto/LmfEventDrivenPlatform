package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import com.lmf.inventory.inventoryservice.domain.model.ReservationStatus;
import com.lmf.inventory.inventoryservice.domain.model.StockReservation;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import com.lmf.inventory.inventoryservice.domain.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationOutcomeServiceTest {

    private ProductRepository productRepository;

    private StockReservationRepository stockReservationRepository;

    private ReservationOutcomeService reservationOutcomeService;

    @BeforeEach
    void setUp() {

        productRepository = mock(ProductRepository.class);
        stockReservationRepository = mock(StockReservationRepository.class);
        reservationOutcomeService = new ReservationOutcomeService(productRepository, stockReservationRepository);
    }

    @Test
    @DisplayName("Deve liberar a reserva (RESERVED -> RELEASED) quando o pagamento falha")
    void shouldReleaseReservationOnPaymentFailure() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = product(productId, 7, 3);
        StockReservation reservation = StockReservation.create(orderId, productId, 3);

        when(stockReservationRepository.findByOrderId(orderId)).thenReturn(List.of(reservation));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        reservationOutcomeService.release(orderId);

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(10);
        assertThat(product.getProductStock().getReservedQuantity()).isZero();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RELEASED);

        verify(productRepository).update(product);
        verify(stockReservationRepository).update(reservation);
    }

    @Test
    @DisplayName("Deve confirmar a reserva (RESERVED -> CONFIRMED) quando o pagamento é aprovado")
    void shouldConfirmReservationOnPaymentApproval() {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = product(productId, 7, 3);
        StockReservation reservation = StockReservation.create(orderId, productId, 3);

        when(stockReservationRepository.findByOrderId(orderId)).thenReturn(List.of(reservation));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        reservationOutcomeService.confirm(orderId);

        assertThat(product.getProductStock().getAvailableQuantity()).isEqualTo(7);
        assertThat(product.getProductStock().getReservedQuantity()).isZero();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Deve ser idempotente — não age sobre reservas já liquidadas")
    void shouldBeIdempotentForAlreadySettledReservations() {

        UUID orderId = UUID.randomUUID();

        StockReservation settled = StockReservation.restore(UUID.randomUUID(), orderId, UUID.randomUUID(), 3, ReservationStatus.RELEASED, OffsetDateTime.now(), OffsetDateTime.now());

        when(stockReservationRepository.findByOrderId(orderId)).thenReturn(List.of(settled));

        reservationOutcomeService.release(orderId);

        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).update(any());
        verify(stockReservationRepository, never()).update(any());
    }

    @Test
    @DisplayName("Não deve fazer nada quando não há reserva para o pedido")
    void shouldDoNothingWhenNoReservationExists() {

        UUID orderId = UUID.randomUUID();

        when(stockReservationRepository.findByOrderId(orderId)).thenReturn(List.of());

        reservationOutcomeService.confirm(orderId);

        verify(productRepository, never()).update(any());
    }

    private Product product(UUID id, int available, int reserved) {

        return Product.restore(id, "SKU-" + id, "Notebook", "Notebook Gamer", BigDecimal.valueOf(5000), available, reserved, ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now());
    }
}
