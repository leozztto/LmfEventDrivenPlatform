package com.lmf.payment.paymentservice.unit.infrasctruture.kafka;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.events.CustomerInfo;
import com.lmf.payment.paymentservice.events.OrderCreatedEvent;
import com.lmf.payment.paymentservice.events.OrderItem;
import com.lmf.payment.paymentservice.events.PaymentInfo;
import com.lmf.payment.paymentservice.events.ShippingAddress;
import com.lmf.payment.paymentservice.infrastructure.kafka.consumer.OrderCreatedDltConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class OrderCreatedDltConsumerTest {

    private OrderCreatedDltConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderCreatedDltConsumer();
    }

    @Test
    void shouldConsumeMessageFromDltWithoutErrors() {

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(UUID.randomUUID(), "ORDER_CREATED", "1.0", OffsetDateTime.now(ZoneOffset.UTC), UUID.randomUUID(), "CREATED", new BigDecimal("299.90"), new CustomerInfo(UUID.randomUUID(), "Leandro Franceschetto", "leandro@email.com", "11999999999"), new ShippingAddress("Rua das Flores", "123", "São Paulo", "SP", "01010-000"),

                new PaymentInfo(PaymentMethod.CREDIT_CARD, 3, new BigDecimal("299.90")),

                List.of(new OrderItem(UUID.randomUUID(), 1, new BigDecimal("250.00")), new OrderItem(UUID.randomUUID(), 1, new BigDecimal("49.90"))));

        assertDoesNotThrow(() -> consumer.consume(orderCreatedEvent));
    }
}