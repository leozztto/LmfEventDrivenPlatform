package com.lmf.notification.notificationservice.domain.model;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Contato para quem notificar sobre um pedido. É populado a partir do {@code OrderCreatedEvent}
 * (o único evento consumido que carrega os dados do cliente) e reusado pelos demais eventos da
 * saga, que só trazem {@code orderId} (e às vezes {@code customerId}).
 */
@Getter
public class NotificationRecipient {

    private UUID orderId;

    private UUID customerId;

    private String name;

    private String email;

    private String phone;

    private OffsetDateTime updatedAt;

    private NotificationRecipient() {
    }

    public static NotificationRecipient of(UUID orderId, UUID customerId, String name, String email, String phone) {

        NotificationRecipient recipient = new NotificationRecipient();
        recipient.orderId = orderId;
        recipient.customerId = customerId;
        recipient.name = name;
        recipient.email = email;
        recipient.phone = phone;
        recipient.updatedAt = OffsetDateTime.now();
        return recipient;
    }

    public static NotificationRecipient restore(UUID orderId, UUID customerId, String name, String email, String phone, OffsetDateTime updatedAt) {

        NotificationRecipient recipient = new NotificationRecipient();
        recipient.orderId = orderId;
        recipient.customerId = customerId;
        recipient.name = name;
        recipient.email = email;
        recipient.phone = phone;
        recipient.updatedAt = updatedAt;
        return recipient;
    }

    public void update(UUID customerId, String name, String email, String phone) {

        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.updatedAt = OffsetDateTime.now();
    }

    /** Melhor endereço disponível para o canal atual (e-mail preferencial, telefone como fallback). */
    public String bestAddress() {

        if (email != null && !email.isBlank()) {
            return email;
        }
        return phone;
    }
}
