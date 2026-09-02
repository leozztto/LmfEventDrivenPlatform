package com.lmf.order.orderservice.application.usecase;

import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.mapper.CreateOrderCommandMapper;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.exception.OrderNotFoundException;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.repository.IdempotencyStore;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.infrastructure.messaging.OrderOutboxWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;

    private final IdempotencyStore idempotencyStore;

    private final OrderOutboxWriter orderOutboxWriter;

    private final CreateOrderCommandMapper createOrderCommandMapper;

    @Transactional
    public CreateOrderResult execute(CreateOrderCommand command) {

        log.info("Creating order. customerId={}, totalItems={}", command.customer().customerId(), command.items().size());

        Optional<UUID> existingOrderId = idempotencyStore.findOrderIdByKey(command.idempotencyKey());

        if (existingOrderId.isPresent()) {

            Order order = orderRepository.findById(existingOrderId.get()).orElseThrow(() -> new OrderNotFoundException(existingOrderId.get()));

            log.info("Idempotent request detected. idempotencyKey={}, orderId={}", command.idempotencyKey(), order.getId());

            return toResult(order);
        }

        Order order = createOrderCommandMapper.toDomain(command);

        // Reserva a chave de idempotência ANTES de persistir o pedido. Numa corrida com a mesma
        // chave, a violação de unique aborta a transação inteira — sem pedido/outbox órfãos — e vira
        // um 409 no GlobalExceptionHandler; o cliente repete e cai no caminho rápido acima.
        idempotencyStore.reserve(command.idempotencyKey(), order.getId());

        Order savedOrder = orderRepository.save(order);

        log.info("Order created. orderId={}, totalAmount={}, status={}", savedOrder.getId(), savedOrder.getTotalAmount(), savedOrder.getOrderStatus());

        orderOutboxWriter.writeOrderCreated(savedOrder);

        return toResult(savedOrder);
    }

    private CreateOrderResult toResult(Order order) {

        return new CreateOrderResult(order.getId(), order.getOrderStatus().name(), order.getTotalAmount(), order.getCreatedAt());
    }
}
