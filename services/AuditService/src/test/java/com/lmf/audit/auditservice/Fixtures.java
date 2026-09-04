package com.lmf.audit.auditservice;

import com.lmf.platform.contracts.CustomerInfo;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.contracts.FraudRejectedEvent;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.InventoryReservedEvent;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.contracts.OrderItem;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
import com.lmf.platform.contracts.PaymentInfo;
import com.lmf.platform.contracts.PaymentMethod;
import com.lmf.platform.contracts.ReservedItem;
import com.lmf.platform.contracts.ShippingAddress;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/** Builders de eventos de contrato para os testes — um por tópico consumido pelo AuditService. */
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

    public static FraudApprovedEvent fraudApproved(UUID orderId, UUID customerId) {

        return new FraudApprovedEvent(
                UUID.randomUUID(), FraudApprovedEvent.TYPE, VERSION, now(),
                orderId, new CustomerInfo(customerId, "Ana Souza", "ana@example.com", "11999998888"),
                new BigDecimal("250.00"), new PaymentInfo(PaymentMethod.PIX, 1, new BigDecimal("250.00")),
                List.of(new OrderItem(UUID.randomUUID(), 2, new BigDecimal("125.00"), new BigDecimal("250.00"))));
    }

    public static FraudRejectedEvent fraudRejected(UUID orderId) {

        return new FraudRejectedEvent(
                UUID.randomUUID(), FraudRejectedEvent.TYPE, VERSION, now(),
                orderId, "Customer is blocklisted");
    }

    public static InventoryReservedEvent inventoryReserved(UUID orderId, UUID customerId) {

        return new InventoryReservedEvent(
                UUID.randomUUID(), InventoryReservedEvent.TYPE, VERSION, now(),
                orderId, customerId, new BigDecimal("250.00"),
                new PaymentInfo(PaymentMethod.PIX, 1, new BigDecimal("250.00")),
                List.of(new ReservedItem(UUID.randomUUID(), 2)));
    }

    public static InventoryReservationFailedEvent inventoryReservationFailed(UUID orderId) {

        return new InventoryReservationFailedEvent(
                UUID.randomUUID(), InventoryReservationFailedEvent.TYPE, VERSION, now(),
                orderId, "Estoque insuficiente para o produto X");
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

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
