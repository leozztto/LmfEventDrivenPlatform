package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.application.usecase.CreateProductUseCase;
import com.lmf.inventory.inventoryservice.application.command.ProductCommand;
import com.lmf.inventory.inventoryservice.domain.exception.ProductAlreadyExistsException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateProductService implements CreateProductUseCase {

    private final ProductRepository productRepository;

    private final InventoryEventService inventoryEventService;

    @Override
    public Product execute(ProductCommand productCommand) {

        if (productRepository.existsBySku(productCommand.sku())) {

            throw new ProductAlreadyExistsException(productCommand.sku());
        }

        Product product = Product.create(productCommand.sku(), productCommand.name(), productCommand.description(), productCommand.price(), productCommand.initialStock());

        productRepository.save(product);

        inventoryEventService.publish(product);

        return product;
    }
}
