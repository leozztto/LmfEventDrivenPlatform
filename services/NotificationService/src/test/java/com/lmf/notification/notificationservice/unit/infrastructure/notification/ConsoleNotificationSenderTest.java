package com.lmf.notification.notificationservice.unit.infrastructure.notification;

import com.lmf.notification.notificationservice.domain.model.NotificationChannel;
import com.lmf.notification.notificationservice.domain.model.NotificationContent;
import com.lmf.notification.notificationservice.infrastructure.notification.ConsoleNotificationSender;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ConsoleNotificationSenderTest {

    private final ConsoleNotificationSender sender = new ConsoleNotificationSender();

    @Test
    void logsWithoutThrowing() {

        NotificationContent content = new NotificationContent(NotificationChannel.LOG, "ana@example.com", "assunto", "corpo");

        assertThatCode(() -> sender.send(content)).doesNotThrowAnyException();
    }
}
