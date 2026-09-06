package com.lmf.gateway.gatewayservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.gateway.gatewayservice.infrastructure.web.exception.RestAccessDeniedHandler;
import com.lmf.gateway.gatewayservice.infrastructure.web.exception.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Segurança na borda: o Gateway valida o JWT como OAuth2 Resource Server (a chave pública vem do
 * JWKS do AuthService, via {@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri}). Rotas
 * de autenticação e de infraestrutura são abertas; {@code /blocklist} e {@code /audit-events}
 * exigem {@code ROLE_ADMIN}; o restante exige apenas um token válido.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(objectMapper);
        RestAccessDeniedHandler accessDeniedHandler = new RestAccessDeniedHandler(objectMapper);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**", "/oauth2/jwks").permitAll()
                        .requestMatchers("/actuator/**", "/error", "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/webjars/**", "/aggregate/**").permitAll()
                        .requestMatchers("/api/v1/blocklist/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/audit-events/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(entryPoint)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(JwtRolesConverter.jwtAuthenticationConverter())))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }
}
