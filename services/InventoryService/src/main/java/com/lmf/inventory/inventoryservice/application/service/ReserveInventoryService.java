package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.inventory.inventoryservice.domain.exception.InsufficientStockException;
import com.lmf.inventory.inventoryservice.domain.exception.ProductNotFoundException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.StockReservation;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import com.lmf.inventory.inventoryservice.domain.repository.StockReservationRepository;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.InventoryReservedEvent;
import com.lmf.platform.contracts.OrderItem;
import com.lmf.platform.contracts.ReservedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveInventoryService implements ReserveInventoryUseCase {

    private static final String EVENT_VERSION = "v1";

    private final ProductRepository productRepository;

    private final StockReservationRepository stockReservationRepository;

    private final ReserveInventoryEventService reserveInventoryEventService;

    /**
     * Reserva o estoque de todos os itens do pedido de forma atômica. A reserva é feita primeiro em
     * memória sobre os agregados carregados; só depois que todos os itens passam é que os produtos são
     * persistidos, as reservas são registradas e o evento de sucesso é gravado no outbox. Se qualquer
     * item falhar, nenhuma reserva parcial é persistida e um único evento de falha é gravado no
     * outbox — a transação é commitada para que o evento de falha não se perca.
     */
    @Override
    @Transactional
    public void execute(FraudApprovedEvent fraudApprovedEvent) {

        Map<UUID, Product> productsById = new LinkedHashMap<>();

        try {

            for (OrderItem orderItem : fraudApprovedEvent.items()) {

                Product product = productsById.computeIfAbsent(orderItem.productId(), id ->
                        productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id)));

                product.reserveStock(orderItem.quantity());
            }

        } catch (ProductNotFoundException | InsufficientStockException businessFailure) {

            log.warn("Inventory reservation failed. orderId={}, reason={}", fraudApprovedEvent.orderId(), businessFailure.getMessage());

            reserveInventoryEventService.publishFailure(toFailedEvent(fraudApprovedEvent, businessFailure.getMessage()));

            return;
        }

        productsById.values().forEach(productRepository::update);

        Map<UUID, Integer> quantityByProduct = new LinkedHashMap<>();

        for (OrderItem orderItem : fraudApprovedEvent.items()) {

            quantityByProduct.merge(orderItem.productId(), orderItem.quantity(), Integer::sum);
        }

        quantityByProduct.forEach((productId, quantity) ->
                stockReservationRepository.save(StockReservation.create(fraudApprovedEvent.orderId(), productId, quantity)));

        List<ReservedItem> reservedItems = quantityByProduct.entrySet().stream()
                .map(entry -> new ReservedItem(entry.getKey(), entry.getValue()))
                .toList();

        reserveInventoryEventService.publishSuccess(toReservedEvent(fraudApprovedEvent, reservedItems));

        log.info("Inventory reserved. orderId={}, items={}", fraudApprovedEvent.orderId(), reservedItems.size());
    }

    private InventoryReservedEvent toReservedEvent(FraudApprovedEvent fraudApprovedEvent, List<ReservedItem> reservedItems) {

        UUID customerId = fraudApprovedEvent.customer() != null ? fraudApprovedEvent.customer().customerId() : null;

        return new InventoryReservedEvent(
                UUID.randomUUID(),
                InventoryReservedEvent.TYPE,
                EVENT_VERSION,
                OffsetDateTime.now(),
                fraudApprovedEvent.orderId(),
                customerId,
                fraudApprovedEvent.totalAmount(),
                fraudApprovedEvent.payment(),
                reservedItems);
    }

    private InventoryReservationFailedEvent toFailedEvent(FraudApprovedEvent fraudApprovedEvent, String reason) {

        return new InventoryReservationFailedEvent(
                UUID.randomUUID(),
                InventoryReservationFailedEvent.TYPE,
                EVENT_VERSION,
                OffsetDateTime.now(),
                fraudApprovedEvent.orderId(),
                reason);
    }
}
