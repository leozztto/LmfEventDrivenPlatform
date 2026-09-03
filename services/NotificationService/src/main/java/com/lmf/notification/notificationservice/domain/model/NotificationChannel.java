package com.lmf.notification.notificationservice.domain.model;

/**
 * Canal de entrega. Nesta fase só {@link #LOG} é usado (envio fake via console);
 * {@link #EMAIL} e {@link #SMS} ficam para adapters posteriores.
 */
public enum NotificationChannel {

    LOG,
    EMAIL,
    SMS
}
