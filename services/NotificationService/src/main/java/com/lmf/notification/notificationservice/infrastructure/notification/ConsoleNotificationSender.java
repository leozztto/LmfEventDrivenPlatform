package com.lmf.notification.notificationservice.infrastructure.notification;

import com.lmf.notification.notificationservice.application.port.out.NotificationSender;
import com.lmf.notification.notificationservice.domain.model.NotificationContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter fake desta fase: "envia" a notificação escrevendo no log. Um {@code EmailNotificationSender}
 * / {@code SmsNotificationSender} entra depois como {@code @Primary}/{@code @ConditionalOnProperty},
 * sem mudar o {@code NotificationDispatchService}.
 */
@Slf4j
@Component
public class ConsoleNotificationSender implements NotificationSender {

    @Override
    public void send(NotificationContent content) {

        log.info("[NOTIFICATION] channel={} to={} | {} — {}",
                content.channel(), content.recipient(), content.subject(), content.body());
    }
}
