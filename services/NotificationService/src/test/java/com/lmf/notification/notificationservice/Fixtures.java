package com.lmf.notification.notificationservice;

import com.lmf.platform.contracts.CustomerInfo;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.contracts.OrderItem;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
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

    public static OrderCreatedEvent orderCreated(UUID orderId, UUID customerId) {

        return new OrderCreatedEvent(
                UUID.randomUUID(), OrderCreatedEvent.TYPE, VERSION, now(),
                orderId, "PENDING_PAYMENT", new BigDecimal("250.00"),
                new CustomerInfo(customerId, "Ana Souza", "ana@example.com", "11999998888"),
                new ShippingAddress("Rua A", "10", "São Paulo", "01000-000", "BR"),
                new PaymentInfo(PaymentMethod.PIX, 1, new BigDecimal("250.00")),
                List.of(new OrderItem(UUID.randomUUID(), 2, new BigDecimal("125.00"), new BigDecimal("250.00"))));
    }

    public static OrderCreatedEvent orderCreatedWithoutCustomer(UUID orderId) {

        return new OrderCreatedEvent(
                UUID.randomUUID(), OrderCreatedEvent.TYPE, VERSION, now(),
                orderId, "PENDING_PAYMENT", new BigDecimal("99.90"),
                null, null, null,
                List.of(new OrderItem(UUID.randomUUID(), 1, new BigDecimal("99.90"), new BigDecimal("99.90"))));
    }

    public static PaymentApprovedEvent paymentApproved(UUID orderId, UUID customerId) {

        return new PaymentApprovedEvent(
                UUID.randomUUID(), PaymentApprovedEvent.TYPE, VERSION, now(),
                UUID.randomUUID(), orderId, customerId,
                new BigDecimal("250.00"), "BRL", PaymentMethod.PIX, "tx-123", "FAKE");
    }

    public static PaymentFailedEvent paymentFailed(UUID orderId, UUID customerId) {

        return new PaymentFailedEvent(
                UUID.randomUUID(), PaymentFailedEvent.TYPE, VERSION, now(),
                UUID.randomUUID(), orderId, customerId,
                new BigDecimal("250.00"), "BRL", PaymentMethod.PIX, "Saldo insuficiente", "DECLINED");
    }

    public static InventoryReservationFailedEvent inventoryReservationFailed(UUID orderId) {

        return new InventoryReservationFailedEvent(
                UUID.randomUUID(), InventoryReservationFailedEvent.TYPE, VERSION, now(),
                orderId, "Estoque insuficiente para o produto X");
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
