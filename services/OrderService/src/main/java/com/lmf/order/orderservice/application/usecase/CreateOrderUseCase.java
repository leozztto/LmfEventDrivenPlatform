package com.lmf.order.orderservice.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.mapper.CreateOrderCommandMapper;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.event.OrderCreatedEvent;
import com.lmf.order.orderservice.domain.exception.OrderNotFoundException;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.model.outbox.OutboxStatus;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.messaging.mapper.OrderCreatedEventMapper;
import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.order.orderservice.infrastructure.persistence.repository.IdempotencyRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final IdempotencyRepositoryAdapter idempotencyRepository;

    private final ObjectMapper objectMapper;

    private final OrderCreatedEventMapper orderCreatedEventMapper;

    private final CreateOrderCommandMapper createOrderCommandMapper;

    @Transactional
    public CreateOrderResult execute(CreateOrderCommand command) {

        log.info("Creating order. customerInfo={}, shippingAddress={}, payment={}, totalItems={}", command.customer(), command.shippingAddress(), command.payment(), command.items().size());

        Optional<IdempotencyEntity> existing = idempotencyRepository.findByKey(command.idempotencyKey());

        if (existing.isPresent()) {

            Order order = orderRepository.findById(existing.get().getOrderId()).orElseThrow(() -> new OrderNotFoundException(existing.get().getOrderId()));

            log.info("Idempotent request detected. idempotencyKey={}, orderId={}", command.idempotencyKey(), order.getId());

            return new CreateOrderResult(order.getId(), order.getOrderStatus().name(), order.getTotalAmount(), order.getCreatedAt());
        }

        Order order = createOrderCommandMapper.toDomain(command);

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully. orderId={}, totalAmount={}, status={}", savedOrder.getId(), savedOrder.getTotalAmount(), savedOrder.getOrderStatus());

        try {
            idempotencyRepository.save(new IdempotencyEntity(command.idempotencyKey(), savedOrder.getId()));
        } catch (DataIntegrityViolationException e) {

            IdempotencyEntity fallback = idempotencyRepository.findByKey(command.idempotencyKey()).orElseThrow();

            Order orderError = orderRepository.findById(fallback.getOrderId()).orElseThrow(() -> new OrderNotFoundException(fallback.getOrderId()));

            return new CreateOrderResult(orderError.getId(), orderError.getOrderStatus().name(), orderError.getTotalAmount(), orderError.getCreatedAt());
        }

        saveOutboxEvent(savedOrder);

        return new CreateOrderResult(savedOrder.getId(), savedOrder.getOrderStatus().name(), savedOrder.getTotalAmount(), savedOrder.getCreatedAt());
    }

    private void saveOutboxEvent(Order order) {

        try {

            OrderCreatedEvent orderCreatedEvent = orderCreatedEventMapper.toEvent(order);

            String payload = objectMapper.writeValueAsString(orderCreatedEvent);

            OutboxEventEntity event = new OutboxEventEntity(order.getId(), "ORDER", "ORDER_CREATED", payload, OutboxStatus.PENDING);

            outboxEventRepository.save(event);

        } catch (JsonProcessingException ex) {

            throw new RuntimeException("Failed to create outbox event", ex);
        }
    }
}
