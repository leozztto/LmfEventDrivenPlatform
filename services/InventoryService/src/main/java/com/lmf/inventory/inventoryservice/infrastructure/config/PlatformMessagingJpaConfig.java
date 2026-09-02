package com.lmf.inventory.inventoryservice.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Inclui o pacote {@code com.lmf.platform.messaging} (Outbox/Inbox comuns) na varredura de entidades
 * e de repositórios JPA, junto com o pacote do próprio serviço.
 */
@Configuration
@EntityScan(basePackages = {
        "com.lmf.inventory.inventoryservice",
        "com.lmf.platform.messaging"
})
@EnableJpaRepositories(basePackages = {
        "com.lmf.inventory.inventoryservice",
        "com.lmf.platform.messaging"
})
public class PlatformMessagingJpaConfig {
}
