package com.lmf.notification.notificationservice.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Inclui o pacote {@code com.lmf.platform.messaging} (Inbox comum) na varredura de entidades e de
 * repositórios JPA, junto com o pacote do próprio serviço. Sem isto o {@code InboxEventRepository}
 * do {@code platform-messaging} não é registrado e o {@code InboxService} do auto-config não sobe.
 */
@Configuration
@EntityScan(basePackages = {
        "com.lmf.notification.notificationservice",
        "com.lmf.platform.messaging"
})
@EnableJpaRepositories(basePackages = {
        "com.lmf.notification.notificationservice",
        "com.lmf.platform.messaging"
})
public class PlatformMessagingJpaConfig {
}
