package com.lmf.order.orderservice.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.model.Order;
import com.lmf.order.orderservice.domain.model.OrderItem;
import com.lmf.order.orderservice.domain.model.OutboxStatus;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public CreateOrderResult execute(CreateOrderCommand command) {

        List<OrderItem> orderItems = command.items().stream().map(orderItem -> new OrderItem(orderItem.productId(), orderItem.quantity(), orderItem.unitPrice())).toList();

        Order order = new Order(command.customerId(), orderItems);

        Order savedOrder = orderRepository.save(order);

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
