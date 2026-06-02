package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.inventory.inventoryservice.domain.event.InventoryReservationFailedEvent;
import com.lmf.inventory.inventoryservice.domain.event.InventoryReservationSuccessEvent;
import com.lmf.inventory.inventoryservice.domain.event.OrderCreatedEvent;
import com.lmf.inventory.inventoryservice.domain.event.ReservedItem;
import com.lmf.inventory.inventoryservice.domain.event.order.OrderItem;
import com.lmf.inventory.inventoryservice.domain.exception.ProductNotFoundException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveInventoryService implements ReserveInventoryUseCase {

    private final ProductRepository productRepository;

    private final ReserveInventoryEventService reserveInventoryEventService;

    @Override
    @Transactional
    public void execute(OrderCreatedEvent orderCreatedEvent) {

        List<ReservedItem> reservedItems = new ArrayList<>();

        for (OrderItem orderItem : orderCreatedEvent.items()) {

            Product product = null;

            try {

                product = productRepository.findById(orderItem.getProductId()).orElseThrow(() -> new ProductNotFoundException(orderItem.getProductId()));

                product.reserveStock(orderItem.getQuantity());

                productRepository.save(product);

                reservedItems.add(new ReservedItem(product.getId(), orderItem.getQuantity()));

                InventoryReservationSuccessEvent reservedSuccessEvent = new InventoryReservationSuccessEvent(UUID.randomUUID(), "INVENTORY_RESERVED", "1.0", OffsetDateTime.now(), orderCreatedEvent.orderId(), product.getId());

                reserveInventoryEventService.publishSuccess(reservedSuccessEvent);

            } catch (Exception exception) {

                InventoryReservationFailedEvent reservedFailedEvent = new InventoryReservationFailedEvent(UUID.randomUUID(), "INVENTORY_RESERVATION_FAILED", "1.0", OffsetDateTime.now(), orderCreatedEvent.orderId(), orderItem.getProductId(), exception.getMessage());

                reserveInventoryEventService.publishFailure(reservedFailedEvent);

                throw exception;
            }
        }
    }
}
