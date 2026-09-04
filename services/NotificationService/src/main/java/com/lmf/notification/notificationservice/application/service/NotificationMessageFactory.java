package com.lmf.notification.notificationservice.application.service;

import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Renderiza o texto (pt-BR) de cada tipo de notificação a partir do evento de saga.
 */
@Component
public class NotificationMessageFactory {

    public NotificationText orderCreated(OrderCreatedEvent event) {

        return new NotificationText(
                "Pedido recebido",
                "Recebemos o seu pedido %s no valor de %s. Avisaremos assim que o pagamento for processado."
                        .formatted(event.orderId(), money(event.totalAmount())));
    }

    public NotificationText paymentApproved(PaymentApprovedEvent event) {

        return new NotificationText(
                "Pagamento aprovado",
                "O pagamento do pedido %s foi aprovado (%s). Seu pedido seguirá para separação."
                        .formatted(event.orderId(), money(event.amount())));
    }

    public NotificationText paymentFailed(PaymentFailedEvent event) {

        return new NotificationText(
                "Pagamento não aprovado",
                "O pagamento do pedido %s não foi aprovado. Motivo: %s."
                        .formatted(event.orderId(), reasonOrDefault(event.failureReason())));
    }

    public NotificationText inventoryReservationFailed(InventoryReservationFailedEvent event) {

        return new NotificationText(
                "Pedido cancelado por falta de estoque",
                "Não foi possível reservar o estoque do pedido %s. Motivo: %s. O pedido foi cancelado."
                        .formatted(event.orderId(), reasonOrDefault(event.reason())));
    }

    private static String money(BigDecimal amount) {

        if (amount == null) {
            return "valor não informado";
        }
        return "R$ " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String reasonOrDefault(String reason) {

        return (reason == null || reason.isBlank()) ? "não informado" : reason;
    }
}
