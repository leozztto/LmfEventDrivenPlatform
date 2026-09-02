package com.lmf.order.orderservice.infrastructure.messaging.consumer;

import com.lmf.order.orderservice.application.usecase.UpdateOrderStatusUseCase;
import com.lmf.order.orderservice.infrastructure.messaging.KafkaTopics;
import com.lmf.platform.contracts.InventoryReservationFailedEvent;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Fecha a saga do pedido consumindo os desfechos publicados por Inventory e Payment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaConsumer {

    private static final String GROUP_ID = "order-service-group";

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @KafkaListener(topics = KafkaTopics.PAYMENT_APPROVED, groupId = GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.PaymentApprovedEvent")
    public void onPaymentApproved(PaymentApprovedEvent event) {

        log.info("Received payment approved. orderId={}, paymentId={}", event.orderId(), event.paymentId());

        updateOrderStatusUseCase.approvePayment(event.orderId());
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.PaymentFailedEvent")
    public void onPaymentFailed(PaymentFailedEvent event) {

        log.info("Received payment failed. orderId={}, paymentId={}, reason={}", event.orderId(), event.paymentId(), event.failureReason());

        updateOrderStatusUseCase.rejectPayment(event.orderId());
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVATION_FAILED, groupId = GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.InventoryReservationFailedEvent")
    public void onInventoryReservationFailed(InventoryReservationFailedEvent event) {

        log.info("Received inventory reservation failed. orderId={}, reason={}", event.orderId(), event.reason());

        updateOrderStatusUseCase.cancelForInventoryFailure(event.orderId());
    }
}
