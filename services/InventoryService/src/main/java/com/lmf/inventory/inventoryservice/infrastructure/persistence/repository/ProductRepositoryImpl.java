package com.lmf.inventory.inventoryservice.infrastructure.persistence.repository;

import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.repository.ProductRepository;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.entity.ProductEntity;
import com.lmf.inventory.inventoryservice.infrastructure.persistence.mapper.ProductPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final SpringDataProductRepository springDataProductRepository;

    private final ProductPersistenceMapper productPersistenceMapper;

    @Override
    public Product save(Product product) {

        ProductEntity productEntity = productPersistenceMapper.toEntity(product);

        ProductEntity saved = springDataProductRepository.save(productEntity);

        return productPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {

        return springDataProductRepository.findById(id).map(productPersistenceMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {

        return springDataProductRepository.findAll().stream().map(productPersistenceMapper::toDomain).toList();
    }

    @Override
    public Optional<Product> findBySku(String sku) {

        return springDataProductRepository.findBySku(sku).map(productPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {

        return springDataProductRepository.existsBySku(sku);
    }

    @Override
    public Product update(Product product) {

        ProductEntity productEntity = springDataProductRepository.findById(product.getId()).orElseThrow();

        productEntity.updateStock(product.getProductStock().getAvailableQuantity(), product.getProductStock().getReservedQuantity(), OffsetDateTime.now());

        return productPersistenceMapper.toDomain(productEntity);
    }
}
