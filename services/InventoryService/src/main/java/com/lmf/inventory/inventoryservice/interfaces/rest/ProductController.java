package com.lmf.inventory.inventoryservice.interfaces.rest;

import com.lmf.inventory.inventoryservice.application.command.StockMovementCommand;
import com.lmf.inventory.inventoryservice.application.usecase.CreateProductUseCase;
import com.lmf.inventory.inventoryservice.application.usecase.StockMovementUseCase;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.interfaces.rest.request.ProductRequest;
import com.lmf.inventory.inventoryservice.interfaces.rest.request.StockMovementRequest;
import com.lmf.inventory.inventoryservice.interfaces.rest.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;

    private final StockMovementUseCase stockMovementUseCase;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest productRequest) {

        Product product = createProductUseCase.execute(productRequest.toProductCommand());

        ProductResponse productResponse = ProductResponseMapper.from(product);

        return ResponseEntity.created(URI.create("/api/v1/products/" + product.getId())).body(productResponse);
    }

    @PatchMapping("/stock")
    public ResponseEntity<ProductResponse> moveStock( @Valid @RequestBody StockMovementRequest stockMovementRequest) {

        Product product = stockMovementUseCase.execute(new StockMovementCommand(stockMovementRequest.productId(), stockMovementRequest.stockMovementType(), stockMovementRequest.quantity(), stockMovementRequest.stockMovementReason()));

        ProductResponse productResponse = ProductResponseMapper.from(product);

        return ResponseEntity.ok(productResponse);
    }
}
