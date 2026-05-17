package com.lmf.order.orderservice.infrastructure.web.controller;

import com.lmf.order.orderservice.application.usecase.CreateOrderUseCase;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.infrastructure.web.request.CreateOrderRequest;
import com.lmf.order.orderservice.infrastructure.web.response.CreateOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(
            @Valid @RequestBody
            CreateOrderRequest request
    ) {

        var result =
                createOrderUseCase.execute(
                        toCommand(request)
                );

        return new CreateOrderResponse(
                result.orderId(),
                result.status(),
                result.totalAmount()
        );
    }

    private CreateOrderCommand toCommand(
            CreateOrderRequest request
    ) {

        return new CreateOrderCommand(
                request.getCustomerId(),

                request.getItems()
                        .stream()
                        .map(item ->
                                new CreateOrderCommand.OrderItemCommand(
                                        item.getProductId(),
                                        item.getQuantity(),
                                        item.getUnitPrice()
                                )
                        )
                        .toList()
        );
    }
}
