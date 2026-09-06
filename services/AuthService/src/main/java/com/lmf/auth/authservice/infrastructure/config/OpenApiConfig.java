package com.lmf.auth.authservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Auth Service API").version("1.0.0")
                .description("Cadastro/login de usuários e emissão de JWT (RS256) com endpoint JWKS")
                .contact(new Contact().name("Leandro Menegazzo Franceschetto")));
    }
}
