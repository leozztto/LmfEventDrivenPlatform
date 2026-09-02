package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.service.ReservationOutcomeService;
import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.contracts.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compensa (ou confirma) a reserva de estoque a partir do desfecho do pagamento. Idempotência
 * garantida pelo estado das reservas em {@link ReservationOutcomeService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutcomeConsumer {

    private static final String GROUP_ID = "inventory-service-group";

    private final ReservationOutcomeService reservationOutcomeService;

    @Transactional
    @KafkaListener(topics = KafkaTopics.PAYMENT_APPROVED, groupId = GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.PaymentApprovedEvent")
    public void onPaymentApproved(PaymentApprovedEvent event) {

        log.info("Received payment approved — confirming reservation. orderId={}", event.orderId());

        reservationOutcomeService.confirm(event.orderId());
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.PaymentFailedEvent")
    public void onPaymentFailed(PaymentFailedEvent event) {

        log.info("Received payment failed — releasing reservation. orderId={}, reason={}", event.orderId(), event.failureReason());

        reservationOutcomeService.release(event.orderId());
    }
}
