package com.lmf.fraud.fraudservice.application.service;

import com.lmf.fraud.fraudservice.application.usecase.EvaluateFraudUseCase;
import com.lmf.fraud.fraudservice.domain.model.FraudCheck;
import com.lmf.fraud.fraudservice.domain.model.FraudDecision;
import com.lmf.fraud.fraudservice.domain.repository.FraudCheckRepository;
import com.lmf.fraud.fraudservice.domain.service.FraudRulesEvaluator;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.contracts.FraudRejectedEvent;
import com.lmf.platform.contracts.OrderCreatedEvent;
import com.lmf.platform.messaging.OutboxWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Aplica as regras de fraude a um pedido recém-criado, grava o histórico da decisão e publica o
 * evento de saída ({@code fraud.approved} ou {@code fraud.rejected}) via Outbox — tudo na mesma
 * transação, mesmo padrão de {@code CreateOrderUseCase}/{@code ReserveInventoryService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluateFraudService implements EvaluateFraudUseCase {

    private static final String EVENT_VERSION = "v1";

    private final FraudRulesEvaluator fraudRulesEvaluator;

    private final FraudCheckRepository fraudCheckRepository;

    private final OutboxWriter outboxWriter;

    @Override
    @Transactional
    public void execute(OrderCreatedEvent event) {

        UUID customerId = event.customer() != null ? event.customer().customerId() : null;
        String customerEmail = event.customer() != null ? event.customer().email() : null;

        FraudDecision decision = fraudRulesEvaluator.evaluate(customerId, customerEmail, event.totalAmount());

        fraudCheckRepository.save(FraudCheck.record(event.orderId(), customerId, decision, event.totalAmount()));

        if (decision.approved()) {

            log.info("Fraud check approved. orderId={}", event.orderId());

            outboxWriter.write(event.orderId(), "ORDER", FraudApprovedEvent.TYPE, toApprovedEvent(event));

        } else {

            log.info("Fraud check rejected. orderId={}, reason={}", event.orderId(), decision.reason());

            outboxWriter.write(event.orderId(), "ORDER", FraudRejectedEvent.TYPE, toRejectedEvent(event, decision.reason()));
        }
    }

    private FraudApprovedEvent toApprovedEvent(OrderCreatedEvent order) {

        return new FraudApprovedEvent(UUID.randomUUID(), FraudApprovedEvent.TYPE, EVENT_VERSION, OffsetDateTime.now(),
                order.orderId(), order.customer(), order.totalAmount(), order.payment(), order.items());
    }

    private FraudRejectedEvent toRejectedEvent(OrderCreatedEvent order, String reason) {

        return new FraudRejectedEvent(UUID.randomUUID(), FraudRejectedEvent.TYPE, EVENT_VERSION, OffsetDateTime.now(),
                order.orderId(), reason);
    }
}
