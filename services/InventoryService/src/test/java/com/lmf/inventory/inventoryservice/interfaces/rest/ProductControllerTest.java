package com.lmf.inventory.inventoryservice.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.inventory.inventoryservice.application.command.ProductCommand;
import com.lmf.inventory.inventoryservice.application.command.StockMovementCommand;
import com.lmf.inventory.inventoryservice.application.usecase.CreateProductUseCase;
import com.lmf.inventory.inventoryservice.application.usecase.GetProductUseCase;
import com.lmf.inventory.inventoryservice.application.usecase.StockMovementUseCase;
import com.lmf.inventory.inventoryservice.domain.exception.ProductAlreadyExistsException;
import com.lmf.inventory.inventoryservice.domain.exception.ProductNotFoundException;
import com.lmf.inventory.inventoryservice.domain.model.Product;
import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateProductUseCase createProductUseCase;

    @MockitoBean
    private GetProductUseCase getProductUseCase;

    @MockitoBean
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
    @DisplayName("Deve listar produtos")
    void shouldListProducts() throws Exception {

        UUID productId = UUID.randomUUID();

        Product product = Product.restore(productId, "SKU-001", "Notebook", "Gaming Notebook", BigDecimal.valueOf(5000), 10, 0, ProductStatus.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now());

        when(getProductUseCase.listAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(productId.toString())).andExpect(jsonPath("$[0].sku").value("SKU-001"));
    }

    @Test
    @DisplayName("Deve retornar um produto por id")
    void shouldReturnProductById() throws Exception {

        UUID productId = UUID.randomUUID();

        Product product = Product.restore(productId, "SKU-001", "Notebook", "Gaming Notebook", BigDecimal.valueOf(5000), 10, 0, ProductStatus.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now());

        when(getProductUseCase.getById(productId)).thenReturn(product);

        mockMvc.perform(get("/api/v1/products/" + productId)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(productId.toString())).andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando o produto não existe")
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        UUID productId = UUID.randomUUID();

        when(getProductUseCase.getById(productId)).thenThrow(new ProductNotFoundException(productId));

        mockMvc.perform(get("/api/v1/products/" + productId)).andExpect(status().isNotFound());
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

    @Test
    @DisplayName("Deve retornar 400 (VALIDATION_ERROR) quando o corpo de criação de produto é inválido")
    void shouldReturnBadRequestWhenProductRequestIsInvalid() throws Exception {

        String request = """
                { "sku":"", "name":"", "description":"", "price":0, "initialStock":-1 }
                """;

        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Deve retornar 409 quando o produto já existe")
    void shouldReturnConflictWhenProductAlreadyExists() throws Exception {

        when(createProductUseCase.execute(ArgumentMatchers.any(ProductCommand.class)))
                .thenThrow(new ProductAlreadyExistsException("SKU-001"));

        String request = """
                { "sku":"SKU-001", "name":"Notebook", "description":"Gaming Notebook", "price":5000, "initialStock":10 }
                """;

        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("PRODUCT_ALREADY_EXISTS"));
    }
}