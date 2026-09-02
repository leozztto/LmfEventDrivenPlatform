package com.lmf.payment.paymentservice.infrastructure.kafka.consumer;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.infrastructure.kafka.config.KafkaTopics;
import com.lmf.platform.contracts.InventoryReservedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class InventoryReservedConsumer extends AbstractInboxConsumer<InventoryReservedEvent> {

    // TODO(Fase 2): a moeda deveria vir do evento, não fixa.
    private static final String DEFAULT_CURRENCY = "BRL";

    private final ProcessPaymentUseCase processPaymentUseCase;

    public InventoryReservedConsumer(InboxService inboxService, ProcessPaymentUseCase processPaymentUseCase) {

        super(inboxService);

        this.processPaymentUseCase = processPaymentUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED, groupId = "payment-service-group")
    public void consume(InventoryReservedEvent inventoryReservedEvent) {

        process(inventoryReservedEvent, inventoryReservedEvent.orderId(), event ->
                processPaymentUseCase.execute(toPaymentCommand(event)));
    }

    private ProcessPaymentCommand toPaymentCommand(InventoryReservedEvent inventoryReservedEvent) {

        return new ProcessPaymentCommand(
                inventoryReservedEvent.orderId(),
                inventoryReservedEvent.eventId(),
                inventoryReservedEvent.eventType(),
                inventoryReservedEvent.customerId(),
                inventoryReservedEvent.totalAmount(),
                DEFAULT_CURRENCY,
                PaymentMethod.valueOf(inventoryReservedEvent.payment().paymentMethod().name()),
                inventoryReservedEvent.payment().installments());
    }
}
