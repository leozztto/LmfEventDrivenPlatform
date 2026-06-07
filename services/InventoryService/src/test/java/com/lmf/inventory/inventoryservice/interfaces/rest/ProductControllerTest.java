package com.lmf.inventory.inventoryservice.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.inventory.inventoryservice.application.command.ProductCommand;
import com.lmf.inventory.inventoryservice.application.command.StockMovementCommand;
import com.lmf.inventory.inventoryservice.application.usecase.CreateProductUseCase;
import com.lmf.inventory.inventoryservice.application.usecase.StockMovementUseCase;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateProductUseCase createProductUseCase;

    @MockBean
    private StockMovementUseCase stockMovementUseCase;

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() throws Exception {

        UUID productId = UUID.randomUUID();

        Product product = Product.restore(productId, "SKU-001", "Notebook", "Gaming Notebook", BigDecimal.valueOf(5000), 10, 0, ProductStatus.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now());

        when(createProductUseCase.execute(ArgumentMatchers.any(ProductCommand.class))).thenReturn(product);

        String request = """
                {
                  "sku":"SKU-001",
                  "name":"Notebook",
                  "description":"Gaming Notebook",
                  "price":5000,
                  "initialStock":10
                }
                """;

        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isCreated()).andExpect(header().string("Location", "/api/v1/products/" + productId)).andExpect(jsonPath("$.id").value(productId.toString())).andExpect(jsonPath("$.sku").value("SKU-001")).andExpect(jsonPath("$.name").value("Notebook"));
    }

    @Test
    @DisplayName("Should move stock successfully")
    void shouldMoveStockSuccessfully() throws Exception {

        UUID productId = UUID.randomUUID();

        Product product = Product.restore(productId, "SKU-001", "Notebook", "Gaming Notebook", BigDecimal.valueOf(5000), 20, 0, ProductStatus.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now());

        when(stockMovementUseCase.execute(ArgumentMatchers.any(StockMovementCommand.class))).thenReturn(product);

        String request = """
                {
                  "productId":"%s",
                  "stockMovementType":"IN",
                  "quantity":10,
                  "stockMovementReason":"PURCHASE"
                }
                """.formatted(productId);

        mockMvc.perform(patch("/api/v1/products/stock").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(productId.toString())).andExpect(jsonPath("$.sku").value("SKU-001")).andExpect(jsonPath("$.name").value("Notebook"));
    }

    @Test
    @DisplayName("Should return bad request when stock movement request is invalid")
    void shouldReturnBadRequestWhenStockMovementRequestIsInvalid() throws Exception {

        String request = """
                {
                  "productId":null,
                  "stockMovementType":null,
                  "quantity":0,
                  "stockMovementReason":null
                }
                """;

        mockMvc.perform(patch("/api/v1/products/stock").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
    }
}