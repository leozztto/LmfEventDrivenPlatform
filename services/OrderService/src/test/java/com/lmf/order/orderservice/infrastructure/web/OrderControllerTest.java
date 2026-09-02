package com.lmf.order.orderservice.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.order.orderservice.application.usecase.CreateOrderUseCase;
import com.lmf.order.orderservice.application.usecase.GetOrderUseCase;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.exception.OrderNotFoundException;
import com.lmf.order.orderservice.infrastructure.web.controller.OrderController;
import com.lmf.order.orderservice.infrastructure.web.request.CreateOrderRequest;
import com.lmf.order.orderservice.support.factory.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateOrderUseCase createOrderUseCase;

    @MockitoBean
    private GetOrderUseCase getOrderUseCase;

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() throws Exception {

        UUID orderId = UUID.randomUUID();

        CreateOrderRequest request = TestDataFactory.createRequest();

        CreateOrderResult result = new CreateOrderResult(orderId, "PENDING_PAYMENT", BigDecimal.valueOf(200), OffsetDateTime.now());

        when(createOrderUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "idem-123").content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.orderId").value(orderId.toString())).andExpect(jsonPath("$.status").value("PENDING_PAYMENT")).andExpect(jsonPath("$.totalAmount").value(200));

        verify(createOrderUseCase).execute(any());
    }

    @Test
    @DisplayName("Should return bad request when request is invalid")
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

        String invalidRequest = """
                {
                  "items": []
                }
                """;

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key", "idem-123").content(invalidRequest)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should require idempotency key header")
    void shouldRequireIdempotencyKeyHeader() throws Exception {

        CreateOrderRequest request = TestDataFactory.createRequest();

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return the order by id")
    void shouldReturnOrderById() throws Exception {

        com.lmf.order.orderservice.domain.model.order.Order order = TestDataFactory.createOrder();

        when(getOrderUseCase.execute(order.getId())).thenReturn(order);

        mockMvc.perform(get("/api/v1/orders/{orderId}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }

    @Test
    @DisplayName("Should return 404 when the order does not exist")
    void shouldReturnNotFoundWhenOrderMissing() throws Exception {

        UUID missingId = UUID.randomUUID();

        when(getOrderUseCase.execute(missingId)).thenThrow(new OrderNotFoundException(missingId));

        mockMvc.perform(get("/api/v1/orders/{orderId}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
