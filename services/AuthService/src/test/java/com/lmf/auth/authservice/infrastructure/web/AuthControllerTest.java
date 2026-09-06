package com.lmf.auth.authservice.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.auth.authservice.application.usecase.AuthenticateUserUseCase;
import com.lmf.auth.authservice.application.usecase.GetCurrentUserUseCase;
import com.lmf.auth.authservice.application.usecase.RegisterUserUseCase;
import com.lmf.auth.authservice.application.usecase.result.AuthenticationResult;
import com.lmf.auth.authservice.application.usecase.result.RegisterUserResult;
import com.lmf.auth.authservice.application.usecase.result.UserProfileResult;
import com.lmf.auth.authservice.domain.exception.InvalidCredentialsException;
import com.lmf.auth.authservice.domain.exception.UsernameAlreadyExistsException;
import com.lmf.auth.authservice.infrastructure.security.SecurityConfig;
import com.lmf.auth.authservice.infrastructure.web.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @MockitoBean
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void registraComSucesso() throws Exception {
        var result = new RegisterUserResult(UUID.randomUUID(), "alice", "alice@example.com",
                List.of("ROLE_USER"), OffsetDateTime.now());
        when(registerUserUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"username":"alice","email":"alice@example.com","password":"s3cret123"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void registroInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"username":"","email":"nope","password":"123"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void usernameDuplicadoRetorna409() throws Exception {
        when(registerUserUseCase.execute(any())).thenThrow(new UsernameAlreadyExistsException("alice"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"username":"alice","email":"alice@example.com","password":"s3cret123"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void loginRetornaAccessToken() throws Exception {
        when(authenticateUserUseCase.execute(any()))
                .thenReturn(new AuthenticationResult("token-abc", "Bearer", 3600));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"usernameOrEmail":"alice","password":"s3cret123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-abc"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginInvalidoRetorna401() throws Exception {
        when(authenticateUserUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"usernameOrEmail":"alice","password":"errada"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void meSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meComTokenRetornaPerfil() throws Exception {
        when(getCurrentUserUseCase.execute("alice")).thenReturn(new UserProfileResult(
                UUID.randomUUID(), "alice", "alice@example.com", List.of("ROLE_USER"), true, OffsetDateTime.now()));

        mockMvc.perform(get("/api/v1/auth/me").with(jwt().jwt(j -> j.subject("alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }
}
