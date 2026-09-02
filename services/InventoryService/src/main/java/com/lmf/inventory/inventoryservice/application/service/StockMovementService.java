package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.application.command.StockMovementCommand;
import com.lmf.inventory.inventoryservice.application.usecase.StockMovementUseCase;
import com.lmf.inventory.inventoryservice.domain.exception.ProductNotFoundException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.StockMovement;
import com.lmf.inventory.inventoryservice.domain.model.StockMovementType;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import com.lmf.inventory.inventoryservice.domain.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMovementService implements StockMovementUseCase {

    private final ProductRepository productRepository;

    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public Product execute(StockMovementCommand stockMovementCommand) {

        Product product = productRepository.findById(stockMovementCommand.productId())
                .orElseThrow(() -> new ProductNotFoundException(stockMovementCommand.productId()));

        if (stockMovementCommand.stockMovementType() == StockMovementType.IN) {

            product.addStock(stockMovementCommand.quantity());

        } else {

            product.removeStock(stockMovementCommand.quantity());
        }

        Product updated = productRepository.update(product);

        stockMovementRepository.save(StockMovement.record(updated, stockMovementCommand.stockMovementType(),
                stockMovementCommand.stockMovementReason(), stockMovementCommand.quantity()));

        log.info("Stock movement recorded. productId={}, type={}, quantity={}, reason={}, availableAfter={}",
                updated.getId(), stockMovementCommand.stockMovementType(), stockMovementCommand.quantity(),
                stockMovementCommand.stockMovementReason(), updated.getProductStock().getAvailableQuantity());

        return updated;
    }
}
