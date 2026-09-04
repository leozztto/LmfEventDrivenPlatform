package com.lmf.inventory.inventoryservice.infrastructure.kafka.consumer;

import com.lmf.inventory.inventoryservice.application.usecase.ReserveInventoryUseCase;
import com.lmf.inventory.inventoryservice.infrastructure.config.KafkaTopics;
import com.lmf.platform.contracts.FraudApprovedEvent;
import com.lmf.platform.messaging.AbstractInboxConsumer;
import com.lmf.platform.messaging.InboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gatilho da reserva de estoque — passou a ser {@code fraud.approved} (publicado pelo FraudService)
 * em vez de {@code order.created}, para que a reserva só ocorra depois que o pedido passa nas regras
 * de fraude.
 */
@Component
public class FraudApprovedConsumer extends AbstractInboxConsumer<FraudApprovedEvent> {

    private final ReserveInventoryUseCase reserveInventoryUseCase;

    public FraudApprovedConsumer(InboxService inboxService, ReserveInventoryUseCase reserveInventoryUseCase) {

        super(inboxService);

        this.reserveInventoryUseCase = reserveInventoryUseCase;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.FRAUD_APPROVED, groupId = "inventory-service-group")
    public void consume(FraudApprovedEvent event) {

        process(event, event.orderId(), reserveInventoryUseCase::execute);
    }
}
