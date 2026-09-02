package com.lmf.order.orderservice.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {
        "com.lmf.order.orderservice",
        "com.lmf.platform.messaging"
})
@EnableJpaRepositories(basePackages = {
        "com.lmf.order.orderservice",
        "com.lmf.platform.messaging"
})
public class PlatformMessagingJpaConfig {
}
