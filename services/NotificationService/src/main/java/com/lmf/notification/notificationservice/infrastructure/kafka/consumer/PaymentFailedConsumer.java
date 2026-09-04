package com.lmf.notification.notificationservice.infrastructure.kafka.consumer;

import com.lmf.notification.notificationservice.application.usecase.NotifyPaymentFailedUseCase;
import com.lmf.notification.notificationservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.PaymentFailedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentFailedConsumer extends AbstractInboxConsumer<PaymentFailedEvent> {

    private final NotifyPaymentFailedUseCase notifyPaymentFailedUseCase;

    public PaymentFailedConsumer(InboxService inboxService, NotifyPaymentFailedUseCase notifyPaymentFailedUseCase) {

        super(inboxService);

        this.notifyPaymentFailedUseCase = notifyPaymentFailedUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = KafkaTopics.GROUP_ID,
            properties = "spring.json.value.default.type=com.lmf.platform.contracts.PaymentFailedEvent")
    public void consume(PaymentFailedEvent event) {

        process(event, event.orderId(), notifyPaymentFailedUseCase::execute);
    }
}
