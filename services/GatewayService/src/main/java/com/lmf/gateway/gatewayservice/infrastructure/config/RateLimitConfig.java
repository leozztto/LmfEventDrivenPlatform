package com.lmf.gateway.gatewayservice.infrastructure.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    /**
     * Registro de {@code RateLimiter} com a config default vinda das properties. O filtro pede um
     * limiter por chave (subject do JWT ou IP), todos compartilhando essa config.
     */
    @Bean
    public RateLimiterRegistry gatewayRateLimiterRegistry(RateLimitProperties properties) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(properties.getLimitForPeriod())
                .limitRefreshPeriod(properties.getRefreshPeriod())
                .timeoutDuration(properties.getTimeout())
                .build();
        return RateLimiterRegistry.of(config);
    }
}
