package com.lmf.gateway.gatewayservice.infrastructure.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.gateway.gatewayservice.infrastructure.web.exception.ErrorResponse;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting da borda com Resilience4j.
 *
 * <p>O gateway WebMVC é servlet, então este filtro roda antes do roteamento para o downstream. É
 * ordenado logo após a cadeia do Spring Security para conseguir ler o {@code subject} do JWT quando
 * a requisição é autenticada; do contrário usa o IP de origem como chave.
 *
 * <p>Limite em memória e por instância — suficiente para o ambiente didático; produção usaria um
 * backend distribuído (Redis).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(SecurityProperties.DEFAULT_FILTER_ORDER + 10)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterRegistry rateLimiterRegistry;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = resolveKey(request);
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(key);

        if (!rateLimiter.acquirePermission()) {
            log.warn("Rate limit excedido. key={}, path={}", key, request.getRequestURI());
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ErrorResponse.of(
                    429, "TOO_MANY_REQUESTS", "Limite de requisições excedido; tente novamente em instantes",
                    request.getRequestURI()));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return "sub:" + authentication.getName();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
