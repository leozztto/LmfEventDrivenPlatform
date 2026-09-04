package com.lmf.inventory.inventoryservice.application.usecase;

import com.lmf.inventory.inventoryservice.domain.model.Product;

import java.util.List;
import java.util.UUID;

public interface GetProductUseCase {

    Product getById(UUID id);

    List<Product> listAll();
}
