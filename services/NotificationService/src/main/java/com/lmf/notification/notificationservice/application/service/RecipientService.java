package com.lmf.notification.notificationservice.application.service;

import com.lmf.notification.notificationservice.domain.model.NotificationRecipient;
import com.lmf.notification.notificationservice.domain.repository.NotificationRecipientRepository;
import com.lmf.platform.contracts.CustomerInfo;
import com.lmf.platform.contracts.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Mantém o contato de notificação por pedido. É alimentado pelo {@code OrderCreatedEvent} e
 * consultado pelos demais eventos da saga, que não carregam os dados do cliente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipientService {

    private final NotificationRecipientRepository notificationRecipientRepository;

    public NotificationRecipient upsertFromOrder(OrderCreatedEvent event) {

        CustomerInfo customer = event.customer();

        UUID customerId = customer != null ? customer.customerId() : null;
        String name = customer != null ? customer.name() : null;
        String email = customer != null ? customer.email() : null;
        String phone = customer != null ? customer.phone() : null;

        NotificationRecipient recipient = notificationRecipientRepository.findByOrderId(event.orderId())
                .map(existing -> {
                    existing.update(customerId, name, email, phone);
                    return existing;
                })
                .orElseGet(() -> NotificationRecipient.of(event.orderId(), customerId, name, email, phone));

        notificationRecipientRepository.save(recipient);

        return recipient;
    }

    public Optional<NotificationRecipient> resolve(UUID orderId) {

        Optional<NotificationRecipient> recipient = notificationRecipientRepository.findByOrderId(orderId);

        if (recipient.isEmpty()) {
            log.warn("No notification recipient known for order. orderId={}", orderId);
        }

        return recipient;
    }
}
