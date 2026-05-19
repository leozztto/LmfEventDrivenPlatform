package com.lmf.order.orderservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI().info(new Info().title("Order Service API").version("1.0.0").description("Order management microservice using DDD, Clean Architecture, Kafka and Outbox Pattern").contact(new Contact().name("Leandro Menegazzo Franceschetto")));
    }
}
