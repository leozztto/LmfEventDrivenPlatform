package com.lmf.order.orderservice.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.order.orderservice.application.usecase.CreateOrderUseCase;
import com.lmf.order.orderservice.application.usecase.command.CreateOrderCommand;
import com.lmf.order.orderservice.application.usecase.mapper.CreateOrderCommandMapper;
import com.lmf.order.orderservice.application.usecase.result.CreateOrderResult;
import com.lmf.order.orderservice.domain.exception.OrderNotFoundException;
import com.lmf.order.orderservice.domain.model.customer.CustomerInfo;
import com.lmf.order.orderservice.domain.model.customer.ShippingAddress;
import com.lmf.order.orderservice.domain.model.order.Order;
import com.lmf.order.orderservice.domain.model.order.OrderItem;
import com.lmf.order.orderservice.domain.model.payment.PaymentInfo;
import com.lmf.order.orderservice.domain.model.payment.PaymentMethod;
import com.lmf.order.orderservice.domain.repository.OrderRepository;
import com.lmf.order.orderservice.domain.repository.OutboxEventRepository;
import com.lmf.order.orderservice.infrastructure.messaging.mapper.OrderCreatedEventMapper;
import com.lmf.order.orderservice.infrastructure.persistence.entity.IdempotencyEntity;
import com.lmf.order.orderservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.lmf.order.orderservice.infrastructure.persistence.repository.IdempotencyRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private IdempotencyRepositoryAdapter idempotencyRepositoryAdapter;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CreateOrderCommandMapper createOrderCommandMapper;

    @Mock
    private OrderCreatedEventMapper orderCreatedEventMapper;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        String idempotencyKey = "testIntegration";
        UUID customerUUID = UUID.randomUUID();
        UUID productUUID = UUID.randomUUID();

        CreateOrderCommand command = new CreateOrderCommand(

                idempotencyKey,

                new CreateOrderCommand.CustomerCommand(customerUUID, "Leandro", "leandro@email.com", "11999999999"),

                new CreateOrderCommand.ShippingAddressCommand("Rua XPTO", "100", "São Paulo", "01000000", "BR"),

                new CreateOrderCommand.PaymentCommand(PaymentMethod.BOLETO, 3, new BigDecimal(100)),

                List.of(new CreateOrderCommand.OrderItemCommand(productUUID, 2, BigDecimal.TEN)));

        when(idempotencyRepositoryAdapter.findByKey(idempotencyKey)).thenReturn(Optional.empty());

        when(objectMapper.writeValueAsString(any())).thenReturn("{json}");

        Order savedOrder = new Order(

                new CustomerInfo(command.customer().customerId(), command.customer().name(), command.customer().email(), command.customer().phone()),

                new ShippingAddress(command.shippingAddress().street(), command.shippingAddress().number(), command.shippingAddress().city(), command.shippingAddress().zipCode(), command.shippingAddress().country()),

                new PaymentInfo(command.payment().paymentMethod(), command.payment().installments(), command.payment().paidAmount()),

                List.of(new OrderItem(UUID.randomUUID(), 2, BigDecimal.valueOf(100))));

        when(createOrderCommandMapper.toDomain(any(CreateOrderCommand.class))).thenReturn(savedOrder);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        CreateOrderResult createOrderResult = createOrderUseCase.execute(command);

        assertThat(createOrderResult).isNotNull();

        assertThat(createOrderResult.status()).isEqualTo("PENDING_PAYMENT");

        verify(orderRepository).save(any(Order.class));

        verify(idempotencyRepositoryAdapter).save(any(IdempotencyEntity.class));

        verify(outboxEventRepository).save(any(OutboxEventEntity.class));
    }

    @Test
    void shouldReturnExistingOrderWhenIdempotencyKeyExists() {

        UUID orderId = UUID.randomUUID();
        String idempotencyKey = "testIntegration";
        UUID customerUUID = UUID.randomUUID();
        UUID productUUID = UUID.randomUUID();

        CreateOrderCommand command = new CreateOrderCommand(

                idempotencyKey,

                new CreateOrderCommand.CustomerCommand(customerUUID, "Leandro", "leandro@email.com", "11999999999"),

                new CreateOrderCommand.ShippingAddressCommand("Rua XPTO", "100", "São Paulo", "01000000", "BR"),

                new CreateOrderCommand.PaymentCommand(PaymentMethod.APPLE_PAY, 3, new BigDecimal(100)),

                List.of(new CreateOrderCommand.OrderItemCommand(productUUID, 2, BigDecimal.TEN)));

        when(idempotencyRepositoryAdapter.findByKey(idempotencyKey)).thenReturn(Optional.of(new IdempotencyEntity(idempotencyKey, orderId)));

        Order existingOrder = new Order(

                new CustomerInfo(command.customer().customerId(), command.customer().name(), command.customer().email(), command.customer().phone()),

                new ShippingAddress(command.shippingAddress().street(), command.shippingAddress().number(), command.shippingAddress().city(), command.shippingAddress().zipCode(), command.shippingAddress().country()),

                new PaymentInfo(command.payment().paymentMethod(), command.payment().installments(), command.payment().paidAmount()),

                List.of(new OrderItem(UUID.randomUUID(), 2, BigDecimal.valueOf(100))));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        CreateOrderResult result = createOrderUseCase.execute(command);

        assertThat(result).isNotNull();

        verify(orderRepository, never()).save(any());

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSerializationFails() throws Exception {

        String idempotencyKey = "testIntegration";
        UUID customerUUID = UUID.randomUUID();
        UUID productUUID = UUID.randomUUID();

        CreateOrderCommand command = new CreateOrderCommand(

                idempotencyKey,

                new CreateOrderCommand.CustomerCommand(customerUUID, "Leandro", "leandro@email.com", "11999999999"),

                new CreateOrderCommand.ShippingAddressCommand("Rua XPTO", "100", "São Paulo", "01000000", "BR"),

                new CreateOrderCommand.PaymentCommand(PaymentMethod.PIX, 3, new BigDecimal(100)),

                List.of(new CreateOrderCommand.OrderItemCommand(productUUID, 2, BigDecimal.TEN)));

        when(idempotencyRepositoryAdapter.findByKey(any())).thenReturn(Optional.empty());

        Order savedOrder = new Order(

                new CustomerInfo(command.customer().customerId(), command.customer().name(), command.customer().email(), command.customer().phone()),

                new ShippingAddress(command.shippingAddress().street(), command.shippingAddress().number(), command.shippingAddress().city(), command.shippingAddress().zipCode(), command.shippingAddress().country()),

                new PaymentInfo(command.payment().paymentMethod(), command.payment().installments(), command.payment().paidAmount()),

                List.of(new OrderItem(UUID.randomUUID(), 2, BigDecimal.valueOf(100))));

        when(createOrderCommandMapper.toDomain(any(CreateOrderCommand.class))).thenReturn(savedOrder);

        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThatThrownBy(() -> createOrderUseCase.execute(command)).isInstanceOf(RuntimeException.class).hasMessage("Failed to create outbox event");
    }

    @Test
    void shouldThrowOrderNotFoundException() {

        String idempotencyKey = "testIntegration";
        UUID customerUUID = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID productUUID = UUID.randomUUID();

        CreateOrderCommand command = new CreateOrderCommand(

                idempotencyKey,

                new CreateOrderCommand.CustomerCommand(customerUUID, "Leandro", "leandro@email.com", "11999999999"),

                new CreateOrderCommand.ShippingAddressCommand("Rua XPTO", "100", "São Paulo", "01000000", "BR"),

                new CreateOrderCommand.PaymentCommand(PaymentMethod.CREDIT_CARD, 3, new BigDecimal(100)),

                List.of(new CreateOrderCommand.OrderItemCommand(productUUID, 2, BigDecimal.TEN)));

        when(idempotencyRepositoryAdapter.findByKey(any())).thenReturn(Optional.of(new IdempotencyEntity("idem-key", orderId)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createOrderUseCase.execute(command)).isInstanceOf(OrderNotFoundException.class);
    }
}
