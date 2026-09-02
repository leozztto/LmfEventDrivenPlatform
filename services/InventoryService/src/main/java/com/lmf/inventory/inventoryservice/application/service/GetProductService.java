package com.lmf.inventory.inventoryservice.application.service;

import com.lmf.inventory.inventoryservice.application.usecase.GetProductUseCase;
import com.lmf.inventory.inventoryservice.domain.exception.ProductNotFoundException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProductService implements GetProductUseCase {

    private final ProductRepository productRepository;

    @Override
    public Product getById(UUID id) {

        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public List<Product> listAll() {

        return productRepository.findAll();
    }
}
