package com.lmf.order.orderservice.infrastructure.web.controller;

import com.lmf.order.orderservice.application.usecase.CreateOrderUseCase;
import com.lmf.order.orderservice.application.usecase.GetOrderUseCase;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.domain.model.payment.PaymentMethod;
import com.lmf.order.orderservice.infrastructure.web.request.CreateOrderRequest;
import com.lmf.order.orderservice.infrastructure.web.response.CreateOrderResponse;
import com.lmf.order.orderservice.infrastructure.web.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    private final GetOrderUseCase getOrderUseCase;

    @Operation(summary = "Create order", description = "Creates a new order and publishes an event to Kafka")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Order created successfully"), @ApiResponse(responseCode = "400", description = "Validation error"), @ApiResponse(responseCode = "409", description = "Invalid order state")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(@RequestHeader(name = "Idempotency-Key") String idempotencyKey, @Valid @RequestBody CreateOrderRequest createOrderRequest) {

        var command = toCommand(idempotencyKey, createOrderRequest);

        var result = createOrderUseCase.execute(command);

        return new CreateOrderResponse(result.orderId(), result.status(), result.totalAmount(), result.createdAt());
    }

    @Operation(summary = "Get order", description = "Returns an order and its current saga status")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Order found"), @ApiResponse(responseCode = "404", description = "Order not found")})
    @GetMapping("/{orderId}")
    public OrderResponse getById(@PathVariable UUID orderId) {

        return OrderResponse.from(getOrderUseCase.execute(orderId));
    }

    private CreateOrderCommand toCommand(String idempotencyKey, CreateOrderRequest createOrderRequest) {

        return new CreateOrderCommand(

                idempotencyKey,

                new CreateOrderCommand.CustomerCommand(createOrderRequest.customer().customerId(), createOrderRequest.customer().name(), createOrderRequest.customer().email(), createOrderRequest.customer().phone()),

                new CreateOrderCommand.ShippingAddressCommand(createOrderRequest.shippingAddress().street(), createOrderRequest.shippingAddress().number(), createOrderRequest.shippingAddress().city(), createOrderRequest.shippingAddress().zipCode(), createOrderRequest.shippingAddress().country()),

                new CreateOrderCommand.PaymentCommand(PaymentMethod.fromName(createOrderRequest.payment().paymentMethod()), createOrderRequest.payment().installments(), createOrderRequest.payment().amount()),

                createOrderRequest.items().stream().map(item -> new CreateOrderCommand.OrderItemCommand(item.productId(), item.quantity(), item.unitPrice())).toList());
    }
}
