package com.lmf.fraud.fraudservice.infrastructure.kafka.consumer;

import com.lmf.fraud.fraudservice.application.usecase.EvaluateFraudUseCase;
import com.lmf.fraud.fraudservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Primeiro elo depois do OrderService: consome {@code order.created} e decide se o pedido segue
 * para a reserva de estoque ({@code fraud.approved}) ou é cancelado por fraude
 * ({@code fraud.rejected}).
 */
@Component
public class OrderCreatedConsumer extends AbstractInboxConsumer<OrderCreatedEvent> {

    private final EvaluateFraudUseCase evaluateFraudUseCase;

    public OrderCreatedConsumer(InboxService inboxService, EvaluateFraudUseCase evaluateFraudUseCase) {

        super(inboxService);

        this.evaluateFraudUseCase = evaluateFraudUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "fraud-service-group")
    public void consume(OrderCreatedEvent event) {

        process(event, event.orderId(), evaluateFraudUseCase::execute);
    }
}
