package com.lmf.notification.notificationservice.domain.model;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro (append-only) do histórico de notificações. Cada evento de saga consumido gera
 * exatamente uma {@code Notification}, já com o seu desfecho: {@link NotificationStatus#SENT},
 * {@link NotificationStatus#FAILED} (o canal recusou a entrega) ou
 * {@link NotificationStatus#SKIPPED} (destinatário do pedido ainda desconhecido).
 */
@Getter
public class Notification {

    private UUID id;

    private UUID orderId;

    private UUID customerId;

    private NotificationType type;

    private NotificationChannel channel;

    private String recipient;

    private String subject;

    private String body;

    private NotificationStatus status;

    private String failureReason;

    private OffsetDateTime createdAt;

    private OffsetDateTime sentAt;

    private Notification() {
    }

    public static Notification sent(UUID orderId, UUID customerId, NotificationType type, NotificationContent content) {

        Notification notification = base(orderId, customerId, type, content);
        notification.status = NotificationStatus.SENT;
        notification.sentAt = OffsetDateTime.now();
        return notification;
    }

    public static Notification failed(UUID orderId, UUID customerId, NotificationType type, NotificationContent content, String failureReason) {

        Notification notification = base(orderId, customerId, type, content);
        notification.status = NotificationStatus.FAILED;
        notification.failureReason = failureReason;
        return notification;
    }

    public static Notification skipped(UUID orderId, UUID customerId, NotificationType type, NotificationContent content, String reason) {

        Notification notification = base(orderId, customerId, type, content);
        notification.status = NotificationStatus.SKIPPED;
        notification.failureReason = reason;
        return notification;
    }

    public static Notification restore(UUID id, UUID orderId, UUID customerId, NotificationType type,
                                       NotificationChannel channel, String recipient, String subject, String body,
                                       NotificationStatus status, String failureReason,
                                       OffsetDateTime createdAt, OffsetDateTime sentAt) {

        Notification notification = new Notification();
        notification.id = id;
        notification.orderId = orderId;
        notification.customerId = customerId;
        notification.type = type;
        notification.channel = channel;
        notification.recipient = recipient;
        notification.subject = subject;
        notification.body = body;
        notification.status = status;
        notification.failureReason = failureReason;
        notification.createdAt = createdAt;
        notification.sentAt = sentAt;
        return notification;
    }

    private static Notification base(UUID orderId, UUID customerId, NotificationType type, NotificationContent content) {

        Notification notification = new Notification();
        notification.id = UUID.randomUUID();
        notification.orderId = orderId;
        notification.customerId = customerId;
        notification.type = type;
        notification.channel = content.channel();
        notification.recipient = content.recipient();
        notification.subject = content.subject();
        notification.body = content.body();
        notification.createdAt = OffsetDateTime.now();
        return notification;
    }
}
