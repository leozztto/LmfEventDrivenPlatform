package com.lmf.inventory.inventoryservice.application.usecase;

import com.lmf.inventory.inventoryservice.application.command.ProductCommand;
import com.lmf.inventory.inventoryservice.domain.model.Product;

public interface CreateProductUseCase {

    Product execute(ProductCommand productCommand);
}
