package com.lmf.order.orderservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupLogger {

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {

        log.info("OrderService started successfully");
    }
}
