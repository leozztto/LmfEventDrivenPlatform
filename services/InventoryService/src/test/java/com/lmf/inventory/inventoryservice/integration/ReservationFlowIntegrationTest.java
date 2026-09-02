package com.lmf.inventory.inventoryservice.integration;

import com.lmf.inventory.inventoryservice.application.service.ReservationOutcomeService;
import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ReservationStatus;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import com.lmf.inventory.inventoryservice.domain.repository.StockReservationRepository;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.repository.SpringDataProductRepository;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.repository.SpringDataStockReservationRepository;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.contracts.OrderItem;
import com.lmf.platform.messaging.OutboxEvent;
import com.lmf.platform.messaging.OutboxEventRepository;
import com.lmf.platform.messaging.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockReservationRepository stockReservationRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ReserveInventoryUseCase reserveInventoryUseCase;

    @Autowired
    private ReservationOutcomeService reservationOutcomeService;

    @Autowired
    private SpringDataProductRepository springDataProductRepository;

    @Autowired
    private SpringDataStockReservationRepository springDataStockReservationRepository;

    @BeforeEach
    void clean() {
        springDataStockReservationRepository.deleteAll();
        outboxEventRepository.deleteAll();
        springDataProductRepository.deleteAll();
    }

    @Test
    void reservesStockRecordsReservationAndOutboxesEvent_thenReleasesOnPaymentFailure() {

        Product product = productRepository.save(Product.create("SKU-INT-1", "Notebook", "Gamer", BigDecimal.valueOf(5000), 10));

        UUID orderId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), OrderCreatedEvent.TYPE, "v1", OffsetDateTime.now(),
                orderId, "PENDING_PAYMENT", BigDecimal.valueOf(150), null, null, null,
                List.of(new OrderItem(product.getId(), 3, BigDecimal.valueOf(50), BigDecimal.valueOf(150))));

        reserveInventoryUseCase.execute(event);

        Product afterReserve = productRepository.findById(product.getId()).orElseThrow();
        assertThat(afterReserve.getProductStock().getAvailableQuantity()).isEqualTo(7);
        assertThat(afterReserve.getProductStock().getReservedQuantity()).isEqualTo(3);

        assertThat(stockReservationRepository.findByOrderId(orderId))
                .singleElement()
                .satisfies(r -> assertThat(r.getStatus()).isEqualTo(ReservationStatus.RESERVED));

        List<OutboxEvent> outbox = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        assertThat(outbox).singleElement()
                .satisfies(o -> assertThat(o.getEventType()).isEqualTo("INVENTORY_RESERVED"));

        // Pagamento recusado -> compensação libera a reserva.
        reservationOutcomeService.release(orderId);

        Product afterRelease = productRepository.findById(product.getId()).orElseThrow();
        assertThat(afterRelease.getProductStock().getAvailableQuantity()).isEqualTo(10);
        assertThat(afterRelease.getProductStock().getReservedQuantity()).isZero();

        assertThat(stockReservationRepository.findByOrderId(orderId))
                .singleElement()
                .satisfies(r -> assertThat(r.getStatus()).isEqualTo(ReservationStatus.RELEASED));
    }

    @Test
    void insufficientStockOutboxesFailureEventAndKeepsNoReservation() {

        Product product = productRepository.save(Product.create("SKU-INT-2", "Mouse", "Wireless", BigDecimal.valueOf(100), 2));

        UUID orderId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), OrderCreatedEvent.TYPE, "v1", OffsetDateTime.now(),
                orderId, "PENDING_PAYMENT", BigDecimal.valueOf(500), null, null, null,
                List.of(new OrderItem(product.getId(), 5, BigDecimal.valueOf(100), BigDecimal.valueOf(500))));

        reserveInventoryUseCase.execute(event);

        assertThat(productRepository.findById(product.getId()).orElseThrow().getProductStock().getAvailableQuantity()).isEqualTo(2);
        assertThat(stockReservationRepository.findByOrderId(orderId)).isEmpty();

        List<OutboxEvent> outbox = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        assertThat(outbox).singleElement()
                .satisfies(o -> assertThat(o.getEventType()).isEqualTo("INVENTORY_RESERVATION_FAILED"));
    }
}
