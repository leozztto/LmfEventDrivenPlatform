package com.lmf.fraud.fraudservice.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Inclui o pacote {@code com.lmf.platform.messaging} (Outbox/Inbox comuns) na varredura de
 * entidades e de repositórios JPA, junto com o pacote do próprio serviço. Sem isto o
 * {@code OutboxEventRepository}/{@code InboxEventRepository} do {@code platform-messaging} não são
 * registrados e o auto-config (Outbox writer/relay, Inbox service) não sobe.
 */
@Configuration
@EntityScan(basePackages = {
        "com.lmf.fraud.fraudservice",
        "com.lmf.platform.messaging"
})
@EnableJpaRepositories(basePackages = {
        "com.lmf.fraud.fraudservice",
        "com.lmf.platform.messaging"
})
public class PlatformMessagingJpaConfig {
}
