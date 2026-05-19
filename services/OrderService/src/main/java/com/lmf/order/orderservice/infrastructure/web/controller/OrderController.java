package com.lmf.order.orderservice.infrastructure.web.controller;

import com.lmf.order.orderservice.application.usecase.CreateOrderUseCase;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.infrastructure.web.request.CreateOrderRequest;
import com.lmf.order.orderservice.infrastructure.web.response.CreateOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @Operation(summary = "Create order", description = "Creates a new order and publishes an event to Kafka")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Order created successfully"), @ApiResponse(responseCode = "400", description = "Validation error"), @ApiResponse(responseCode = "409", description = "Invalid order state")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(@RequestHeader(name = "Idempotency-Key") String idempotencyKey, @Valid @RequestBody CreateOrderRequest createOrderRequest) {

        var command = toCommand(idempotencyKey, createOrderRequest);

        var result = createOrderUseCase.execute(command);

        return new CreateOrderResponse(result.orderId(), result.status(), result.totalAmount());
    }

    private CreateOrderCommand toCommand(String idempotencyKey, CreateOrderRequest request) {

        return new CreateOrderCommand(idempotencyKey, request.getCustomerId(), request.getItems().stream().map(item -> new CreateOrderCommand.OrderItemCommand(item.getProductId(), item.getQuantity(), item.getUnitPrice())).toList());
    }
}
