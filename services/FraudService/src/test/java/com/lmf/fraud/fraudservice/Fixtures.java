package com.lmf.fraud.fraudservice;

import com.lmf.platform.contracts.CustomerInfo;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.contracts.OrderItem;
import com.lmf.platform.contracts.PaymentInfo;
import com.lmf.platform.contracts.PaymentMethod;
import com.lmf.platform.contracts.ShippingAddress;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/** Builders de eventos de contrato para os testes. */
public final class Fixtures {

    private Fixtures() {
    }

    public static final String VERSION = "v1";

    public static OrderCreatedEvent orderCreated(UUID orderId, UUID customerId, String customerEmail, BigDecimal totalAmount) {

        return new OrderCreatedEvent(
                UUID.randomUUID(), OrderCreatedEvent.TYPE, VERSION, now(),
                orderId, "PENDING_PAYMENT", totalAmount,
                new CustomerInfo(customerId, "Ana Souza", customerEmail, "11999998888"),
                new ShippingAddress("Rua A", "10", "São Paulo", "01000-000", "BR"),
                new PaymentInfo(PaymentMethod.PIX, 1, totalAmount),
                List.of(new OrderItem(UUID.randomUUID(), 2, totalAmount, totalAmount)));
    }

    public static OrderCreatedEvent orderCreated(UUID orderId, UUID customerId) {

        return orderCreated(orderId, customerId, "ana@example.com", new BigDecimal("250.00"));
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
