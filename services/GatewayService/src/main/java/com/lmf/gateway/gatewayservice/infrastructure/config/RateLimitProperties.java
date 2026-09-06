package com.lmf.gateway.gatewayservice.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Parâmetros do rate limiting da borda (Resilience4j {@code RateLimiter}).
 *
 * <p>{@code limitForPeriod} requisições são permitidas a cada {@code refreshPeriod}; requisições
 * além disso esperam até {@code timeout} por uma permissão e, esgotado o tempo, recebem 429.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "gateway.ratelimit")
public class RateLimitProperties {

    private int limitForPeriod = 100;

    private Duration refreshPeriod = Duration.ofSeconds(1);

    private Duration timeout = Duration.ZERO;
}
