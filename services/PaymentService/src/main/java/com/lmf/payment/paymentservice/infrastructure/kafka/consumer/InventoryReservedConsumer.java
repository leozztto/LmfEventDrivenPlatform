package com.lmf.payment.paymentservice.infrastructure.kafka.consumer;

import com.lmf.payment.paymentservice.application.command.ProcessPaymentCommand;
import com.lmf.payment.paymentservice.application.service.InboxEventService;
import com.lmf.payment.paymentservice.application.usecase.ProcessPaymentUseCase;
import com.lmf.payment.paymentservice.events.InventoryReservedEvent;
import com.lmf.payment.paymentservice.infrastructure.kafka.config.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryReservedConsumer extends AbstractInboxConsumer<ProcessPaymentCommand> {

    private final ProcessPaymentUseCase processPaymentUseCase;

    public InventoryReservedConsumer(InboxEventService inboxEventService, ProcessPaymentUseCase processPaymentUseCase) {

        super(inboxEventService);

        this.processPaymentUseCase = processPaymentUseCase;
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED, groupId = "payment-service-group")
    public void consume(InventoryReservedEvent inventoryReservedEvent) {

        ProcessPaymentCommand processPaymentCommand = toPaymentCommand(inventoryReservedEvent);

        process(processPaymentCommand, processPaymentCommand.orderId(), processPaymentUseCase::execute);
    }

    private ProcessPaymentCommand toPaymentCommand(InventoryReservedEvent inventoryReservedEvent) {

        return new ProcessPaymentCommand(inventoryReservedEvent.orderId(), inventoryReservedEvent.eventId(), inventoryReservedEvent.eventType(), inventoryReservedEvent.customerId(), inventoryReservedEvent.totalAmount(), "BRL", inventoryReservedEvent.payment().paymentMethod(), inventoryReservedEvent.payment().installments());
    }
}
