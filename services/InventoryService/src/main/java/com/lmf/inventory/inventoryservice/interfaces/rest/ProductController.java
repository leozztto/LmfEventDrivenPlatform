package com.lmf.inventory.inventoryservice.interfaces.rest;

import com.lmf.inventory.inventoryservice.application.usecase.CreateProductUseCase;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.interfaces.rest.request.ProductRequest;
import com.lmf.inventory.inventoryservice.interfaces.rest.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest productRequest) {

        Product product = createProductUseCase.execute(productRequest.toProductCommand());

        ProductResponse productResponse = ProductResponseMapper.from(product);

        return ResponseEntity.created(URI.create("/api/v1/products/" + product.getId())).body(productResponse);
    }
}
