package com.lmf.order.orderservice.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.exception.OrderNotFoundException;
import com.lmf.order.orderservice.domain.model.Order;
import com.lmf.order.orderservice.domain.model.OrderItem;
import com.lmf.order.orderservice.domain.model.OutboxStatus;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.order.orderservice.infrastructure.persistence.repository.IdempotencyRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final IdempotencyRepositoryAdapter idempotencyRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public CreateOrderResult execute(CreateOrderCommand command) {

        log.info("Creating order. customerId={}, totalItems={}", command.customerId(), command.items().size());

        var existing = idempotencyRepository.findByKey(command.idempotencyKey());

        if (existing.isPresent()) {

            Order existingOrder = orderRepository.findById(existing.get().getOrderId()).orElseThrow(() -> new OrderNotFoundException(existing.get().getOrderId()));

            log.info("Idempotent request detected. idempotencyKey={}, orderId={}", command.idempotencyKey(), existingOrder.getId());

            return new CreateOrderResult(existingOrder.getId(), existingOrder.getOrderStatus().name(), existingOrder.getTotalAmount());
        }

        List<OrderItem> orderItems = command.items().stream().map(item -> new OrderItem(item.productId(), item.quantity(), item.unitPrice())).toList();

        Order order = new Order(command.customerId(), orderItems);

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully. orderId={}, customerId={}, totalAmount={}, status={}", savedOrder.getId(), savedOrder.getCustomerId(), savedOrder.getTotalAmount(), savedOrder.getOrderStatus());

        idempotencyRepository.save(new IdempotencyEntity(command.idempotencyKey(), savedOrder.getId()));

        saveOutboxEvent(savedOrder);

        return new CreateOrderResult(savedOrder.getId(), savedOrder.getOrderStatus().name(), savedOrder.getTotalAmount());
    }

    private void saveOutboxEvent(Order order) {

        try {

            String payload = objectMapper.writeValueAsString(order);

            OutboxEventEntity event = new OutboxEventEntity(order.getId(), "ORDER", "ORDER_CREATED", payload, OutboxStatus.PENDING);

            outboxEventRepository.save(event);

        } catch (JsonProcessingException ex) {

            throw new RuntimeException("Failed to create outbox event", ex);
        }
    }
}
