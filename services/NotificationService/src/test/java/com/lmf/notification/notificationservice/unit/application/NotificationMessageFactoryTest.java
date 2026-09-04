package com.lmf.notification.notificationservice.unit.application;

import com.lmf.notification.notificationservice.Fixtures;
import com.lmf.notification.notificationservice.application.service.NotificationMessageFactory;
import com.lmf.notification.notificationservice.application.service.NotificationText;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
import com.lmf.platform.contracts.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMessageFactoryTest {

    private final NotificationMessageFactory factory = new NotificationMessageFactory();

    @Test
    void orderCreatedMentionsOrderAndAmount() {

        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = Fixtures.orderCreated(orderId, UUID.randomUUID());

        NotificationText text = factory.orderCreated(event);

        assertThat(text.subject()).isEqualTo("Pedido recebido");
        assertThat(text.body()).contains(orderId.toString()).contains("R$ 250.00");
    }

    @Test
    void paymentApprovedMentionsOrderAndAmount() {

        UUID orderId = UUID.randomUUID();
        PaymentApprovedEvent event = Fixtures.paymentApproved(orderId, UUID.randomUUID());

        NotificationText text = factory.paymentApproved(event);

        assertThat(text.subject()).isEqualTo("Pagamento aprovado");
        assertThat(text.body()).contains(orderId.toString()).contains("R$ 250.00");
    }

    @Test
    void paymentFailedMentionsReason() {

        PaymentFailedEvent event = Fixtures.paymentFailed(UUID.randomUUID(), UUID.randomUUID());

        NotificationText text = factory.paymentFailed(event);

        assertThat(text.subject()).isEqualTo("Pagamento não aprovado");
        assertThat(text.body()).contains("Saldo insuficiente");
    }

    @Test
    void inventoryReservationFailedMentionsReason() {

        InventoryReservationFailedEvent event = Fixtures.inventoryReservationFailed(UUID.randomUUID());

        NotificationText text = factory.inventoryReservationFailed(event);

        assertThat(text.subject()).isEqualTo("Pedido cancelado por falta de estoque");
        assertThat(text.body()).contains("Estoque insuficiente para o produto X");
    }

    @Test
    void orderCreatedToleratesMissingAmount() {

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(), OrderCreatedEvent.TYPE, "v1", OffsetDateTime.now(),
                UUID.randomUUID(), "PENDING_PAYMENT", null, null, null, null, java.util.List.of());

        assertThat(factory.orderCreated(event).body()).contains("valor não informado");
    }

    @Test
    void paymentFailedUsesDefaultWhenReasonBlank() {

        PaymentFailedEvent event = new PaymentFailedEvent(
                UUID.randomUUID(), PaymentFailedEvent.TYPE, "v1", OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("10.00"), "BRL", PaymentMethod.PIX, "   ", "DECLINED");

        assertThat(factory.paymentFailed(event).body()).contains("Motivo: não informado.");
    }

    @Test
    void inventoryReservationFailedUsesDefaultWhenReasonNull() {

        InventoryReservationFailedEvent event = new InventoryReservationFailedEvent(
                UUID.randomUUID(), InventoryReservationFailedEvent.TYPE, "v1", OffsetDateTime.now(),
                UUID.randomUUID(), null);

        assertThat(factory.inventoryReservationFailed(event).body()).contains("Motivo: não informado.");
    }

    @Test
    void paymentApprovedRoundsAmountToTwoDecimals() {

        PaymentApprovedEvent event = new PaymentApprovedEvent(
                UUID.randomUUID(), PaymentApprovedEvent.TYPE, "v1", OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("99.9"), "BRL", PaymentMethod.PIX, "tx-1", "FAKE");

        assertThat(factory.paymentApproved(event).body()).contains("R$ 99.90");
    }
}
