package com.lmf.gateway.gatewayservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Platform API Gateway").version("1.0.0")
                .description("Ponto único de entrada: roteamento, validação de JWT na borda, rate limiting e agregação de OpenAPI")
                .contact(new Contact().name("Leandro Menegazzo Franceschetto")));
    }
}
