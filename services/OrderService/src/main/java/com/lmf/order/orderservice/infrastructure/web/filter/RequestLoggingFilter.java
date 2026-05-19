package com.lmf.order.orderservice.infrastructure.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        long start = System.currentTimeMillis();

        try {

            log.info("HTTP request started. method={}, path={}, correlationId={}", request.getMethod(), request.getRequestURI(), MDC.get("correlationId"));

            filterChain.doFilter(request, response);

        } finally {

            long duration = System.currentTimeMillis() - start;

            log.info("HTTP request finished. method={}, path={}, status={}, durationMs={}", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        }
    }
}
