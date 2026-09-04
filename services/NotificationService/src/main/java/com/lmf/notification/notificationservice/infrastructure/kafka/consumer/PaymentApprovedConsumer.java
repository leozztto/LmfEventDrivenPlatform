package com.lmf.notification.notificationservice.infrastructure.kafka.consumer;

import com.lmf.notification.notificationservice.application.usecase.NotifyPaymentApprovedUseCase;
import com.lmf.notification.notificationservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.PaymentApprovedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Novo consumidor de {@code payment.approved} (junto com Order e Inventory) — prova o fan-out da
 * coreografia sem tocar no PaymentService.
 */
@Component
public class PaymentApprovedConsumer extends AbstractInboxConsumer<PaymentApprovedEvent> {

    private final NotifyPaymentApprovedUseCase notifyPaymentApprovedUseCase;

    public PaymentApprovedConsumer(InboxService inboxService, NotifyPaymentApprovedUseCase notifyPaymentApprovedUseCase) {

        super(inboxService);

        this.notifyPaymentApprovedUseCase = notifyPaymentApprovedUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.PAYMENT_APPROVED, groupId = KafkaTopics.GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.PaymentApprovedEvent")
    public void consume(PaymentApprovedEvent event) {

        process(event, event.orderId(), notifyPaymentApprovedUseCase::execute);
    }
}
