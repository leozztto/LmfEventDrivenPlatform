package com.lmf.notification.notificationservice.application.service;

/**
 * Texto renderizado de uma notificação, antes de escolher canal/destinatário.
 */
public record NotificationText(String subject, String body) {
}
