package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.domain.exception.ProductNotFoundException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.StockReservation;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import com.lmf.inventory.inventoryservice.domain.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Aplica o desfecho do pagamento às reservas de estoque de um pedido, completando o ciclo
 * {@code RESERVED -> RELEASED | CONFIRMED}.
 * <p>
 * É idempotente por estado: só age sobre reservas ainda {@code RESERVED}. Uma reentrega do evento
 * (ou os dois desfechos chegando fora de ordem) não desfaz o que já foi aplicado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationOutcomeService {

    private final ProductRepository productRepository;

    private final StockReservationRepository stockReservationRepository;

    @Transactional
    public void confirm(UUID orderId) {

        apply(orderId, "payment approved", (product, reservation) -> {
            product.confirmReservedStock(reservation.getQuantity());
            reservation.markConfirmed();
        });
    }

    @Transactional
    public void release(UUID orderId) {

        apply(orderId, "payment failed", (product, reservation) -> {
            product.releaseStock(reservation.getQuantity());
            reservation.markReleased();
        });
    }

    private void apply(UUID orderId, String reason, BiConsumer<Product, StockReservation> outcome) {

        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);

        if (reservations.isEmpty()) {

            log.warn("No stock reservation found for order. orderId={}, reason={}", orderId, reason);

            return;
        }

        List<StockReservation> pending = reservations.stream().filter(StockReservation::isPending).toList();

        if (pending.isEmpty()) {

            log.info("Stock reservations already settled for order. orderId={}, reason={}", orderId, reason);

            return;
        }

        for (StockReservation reservation : pending) {

            Product product = productRepository.findById(reservation.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(reservation.getProductId()));

            outcome.accept(product, reservation);

            productRepository.update(product);

            stockReservationRepository.update(reservation);
        }

        log.info("Stock reservations settled. orderId={}, count={}, reason={}", orderId, pending.size(), reason);
    }
}
