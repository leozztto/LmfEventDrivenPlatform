package com.lmf.notification.notificationservice.domain.model;

/**
 * O que é entregue ao {@code NotificationSender}: canal, destinatário e o texto já renderizado.
 */
public record NotificationContent(

        NotificationChannel channel,

        String recipient,

        String subject,

        String body) {
}
