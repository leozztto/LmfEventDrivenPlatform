package com.lmf.auth.authservice.infrastructure.web.controller;

import com.lmf.auth.authservice.application.usecase.AuthenticateUserUseCase;
import com.lmf.auth.authservice.application.usecase.GetCurrentUserUseCase;
import com.lmf.auth.authservice.application.usecase.RegisterUserUseCase;
import com.lmf.auth.authservice.application.usecase.command.AuthenticateUserCommand;
import com.lmf.auth.authservice.application.usecase.command.RegisterUserCommand;
import com.lmf.auth.authservice.infrastructure.web.request.LoginRequest;
import com.lmf.auth.authservice.infrastructure.web.request.RegisterRequest;
import com.lmf.auth.authservice.infrastructure.web.response.AuthResponse;
import com.lmf.auth.authservice.infrastructure.web.response.RegisterResponse;
import com.lmf.auth.authservice.infrastructure.web.response.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Cadastro, login e emissão de JWT")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @Operation(summary = "Cadastra um novo usuário (papel ROLE_USER)")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        var command = new RegisterUserCommand(request.username(), request.email(), request.password());
        return RegisterResponse.from(registerUserUseCase.execute(command));
    }

    @Operation(summary = "Autentica e devolve um access token JWT")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var command = new AuthenticateUserCommand(request.usernameOrEmail(), request.password());
        return AuthResponse.from(authenticateUserUseCase.execute(command));
    }

    @Operation(summary = "Retorna o perfil do usuário autenticado (a partir do token)")
    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserProfileResponse.from(getCurrentUserUseCase.execute(jwt.getSubject()));
    }
}
