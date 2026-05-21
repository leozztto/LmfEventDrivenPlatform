package com.lmf.order.orderservice.integration;

import com.lmf.order.orderservice.application.usecase.CreateOrderUseCase;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.model.payment.PaymentMethod;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.order.orderservice.infrastructure.persistence.repository.IdempotencyRepositoryAdapter;
import com.lmf.order.orderservice.infrastructure.persistence.repository.SpringDataIdempotencyRepository;
import com.lmf.order.orderservice.infrastructure.persistence.repository.SpringDataOrderRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private SpringDataIdempotencyRepository springDataIdempotencyRepository;

    @Autowired
    private SpringDataOrderRepository springDataOrderRepository;

    @BeforeEach
    void setup() {
        springDataOrderRepository.deleteAll();
        springDataIdempotencyRepository.deleteAll();
    }

    @Test
    void shouldCreateOrderAndPersistOutboxEvent() {

        String idempotencyKey = "testIntegration";
        UUID customerUUID = UUID.randomUUID();

        CreateOrderCommand command = new CreateOrderCommand(

                idempotencyKey,

                new CreateOrderCommand.CustomerCommand(customerUUID, "Leandro", "leandro@email.com", "11999999999"),

                new CreateOrderCommand.ShippingAddressCommand("Rua XPTO", "100", "São Paulo", "01000000", "BR"),

                new CreateOrderCommand.PaymentCommand(PaymentMethod.PAYPAL, 3, new BigDecimal(10)),

                List.of(new CreateOrderCommand.OrderItemCommand(UUID.randomUUID(), 2, BigDecimal.TEN)));

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

        CreateOrderCommand command = new CreateOrderCommand(

                idempotencyKey,

                new CreateOrderCommand.CustomerCommand(customerUUID, "Leandro", "leandro@email.com", "11999999999"),

                new CreateOrderCommand.ShippingAddressCommand("Rua XPTO", "100", "São Paulo", "01000000", "BR"),

                new CreateOrderCommand.PaymentCommand(PaymentMethod.BOLETO, 3, new BigDecimal(5)),

                List.of(new CreateOrderCommand.OrderItemCommand(UUID.randomUUID(), 2, BigDecimal.TEN)));

        CreateOrderResult firstResult = createOrderUseCase.execute(command);

        CreateOrderResult secondResult = createOrderUseCase.execute(command);

        assertThat(firstResult.orderId()).isEqualTo(secondResult.orderId());

        assertThat(firstResult.status()).isEqualTo(secondResult.status());

        assertThat(firstResult.totalAmount()).isEqualByComparingTo(secondResult.totalAmount());

        assertThat(idempotencyRepositoryAdapter.findByKey(idempotencyKey)).isPresent();
    }
}
