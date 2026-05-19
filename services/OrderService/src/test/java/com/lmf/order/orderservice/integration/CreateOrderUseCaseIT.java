package com.lmf.order.orderservice.integration;

import com.lmf.order.orderservice.application.usecase.CreateOrderUseCase;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.model.Order;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.order.orderservice.infrastructure.persistence.repository.IdempotencyRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CreateOrderUseCaseIT {

    @Autowired
    private CreateOrderUseCase createOrderUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private IdempotencyRepositoryAdapter idempotencyRepositoryAdapter;

    @Test
    void shouldCreateOrderAndPersistOutboxEvent() {

        String idempotencyKey = "testIntegration";
        UUID customerUUID = UUID.randomUUID();

        CreateOrderCommand command = new CreateOrderCommand(idempotencyKey, customerUUID, List.of(new CreateOrderCommand.OrderItemCommand(UUID.randomUUID(), 2, BigDecimal.TEN)));

        CreateOrderResult createOrderResult = createOrderUseCase.execute(command);

        assertThat(createOrderResult).isNotNull();

        Order order = orderRepository.findById(createOrderResult.orderId()).orElseThrow();

        assertThat(order.getOrderItems()).hasSize(1);

        List<OutboxEventEntity> outboxEventEntities = outboxEventRepository.findAll();

        assertThat(outboxEventEntities).hasSizeGreaterThan(0);

        Optional<IdempotencyEntity> idem = idempotencyRepositoryAdapter.findByKey(idempotencyKey);

        assertThat(idem).isPresent();
    }

    @Test
    void shouldNotCreateDuplicateOrder() {

        String idempotencyKey = "testIntegration";
        UUID customerUUID = UUID.randomUUID();

        CreateOrderCommand command = new CreateOrderCommand(idempotencyKey, customerUUID, List.of(new CreateOrderCommand.OrderItemCommand(UUID.randomUUID(), 2, BigDecimal.TEN)));

        CreateOrderResult firstResult = createOrderUseCase.execute(command);

        CreateOrderResult secondResult = createOrderUseCase.execute(command);

        assertThat(firstResult.orderId()).isEqualTo(secondResult.orderId());

        assertThat(firstResult.status()).isEqualTo(secondResult.status());

        assertThat(firstResult.totalAmount()).isEqualByComparingTo(secondResult.totalAmount());

        assertThat(idempotencyRepositoryAdapter.findByKey(idempotencyKey)).isPresent();
    }
}
