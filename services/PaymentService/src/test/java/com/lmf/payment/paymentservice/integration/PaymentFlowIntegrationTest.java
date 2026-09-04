package com.lmf.payment.paymentservice.integration;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import com.lmf.payment.paymentservice.domain.repository.PaymentRepository;
import com.lmf.platform.messaging.OutboxEvent;
import com.lmf.platform.messaging.OutboxEventRepository;
import com.lmf.platform.messaging.OutboxRelay;
import com.lmf.platform.messaging.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProcessPaymentUseCase processPaymentUseCase;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxRelay outboxRelay;

    @BeforeEach
    void clean() {
        outboxEventRepository.deleteAll();
    }

    @Test
    void approvesPaymentPersistsAndOutboxesApprovedEvent_thenRelayPublishes() {

        UUID orderId = UUID.randomUUID();

        processPaymentUseCase.execute(new ProcessPaymentCommand(orderId, UUID.randomUUID(), "INVENTORY_RESERVED",
                UUID.randomUUID(), new BigDecimal("120.00"), "BRL", PaymentMethod.CREDIT_CARD, 1));

        assertThat(paymentRepository.findByOrderId(orderId)).get()
                .satisfies(p -> assertThat(p.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED));

        List<OutboxEvent> pending = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        assertThat(pending).singleElement()
                .satisfies(o -> assertThat(o.getEventType()).isEqualTo("PAYMENT_APPROVED"));

        outboxRelay.process();

        assertThat(outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).isEmpty();
        assertThat(outboxEventRepository.findAll()).singleElement()
                .satisfies(o -> assertThat(o.getStatus()).isEqualTo(OutboxStatus.PUBLISHED));
    }

    @Test
    void declinedPaymentPersistsAsFailedAndOutboxesFailedEvent() {

        UUID orderId = UUID.randomUUID();

        processPaymentUseCase.execute(new ProcessPaymentCommand(orderId, UUID.randomUUID(), "INVENTORY_RESERVED",
                UUID.randomUUID(), new BigDecimal("50000.00"), "BRL", PaymentMethod.CREDIT_CARD, 1));

        assertThat(paymentRepository.findByOrderId(orderId)).get()
                .satisfies(p -> assertThat(p.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED));

        assertThat(outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)).singleElement()
                .satisfies(o -> assertThat(o.getEventType()).isEqualTo("PAYMENT_FAILED"));
    }
}
