package com.lmf.audit.auditservice.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Inclui o pacote {@code com.lmf.platform.messaging} (Outbox/Inbox comuns) na varredura de
 * entidades e de repositórios JPA, junto com o pacote do próprio serviço. Sem isto o
 * {@code InboxEventRepository} do {@code platform-messaging} não é registrado e o auto-config
 * (Inbox service) não sobe.
 */
@Configuration
@EntityScan(basePackages = {
        "com.lmf.audit.auditservice",
        "com.lmf.platform.messaging"
})
@EnableJpaRepositories(basePackages = {
        "com.lmf.audit.auditservice",
        "com.lmf.platform.messaging"
})
public class PlatformMessagingJpaConfig {
}
