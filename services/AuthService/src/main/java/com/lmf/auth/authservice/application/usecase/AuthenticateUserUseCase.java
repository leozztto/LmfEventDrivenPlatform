package com.lmf.auth.authservice.application.usecase;

import com.lmf.auth.authservice.application.port.TokenIssuer;
import com.lmf.auth.authservice.application.usecase.command.AuthenticateUserCommand;
import com.lmf.auth.authservice.application.usecase.result.AuthenticationResult;
import com.lmf.auth.authservice.domain.exception.InvalidCredentialsException;
import com.lmf.auth.authservice.domain.model.user.User;
import com.lmf.auth.authservice.domain.repository.UserRepository;
import com.lmf.auth.authservice.domain.service.PasswordHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    @Transactional(readOnly = true)
    public AuthenticationResult execute(AuthenticateUserCommand command) {
        log.info("Autenticando. usernameOrEmail={}", command.usernameOrEmail());

        User user = userRepository.findByUsernameOrEmail(command.usernameOrEmail())
                .orElseThrow(InvalidCredentialsException::new);

        user.ensureEnabled();

        if (!passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        TokenIssuer.IssuedToken issued = tokenIssuer.issue(user);
        long expiresIn = Duration.between(Instant.now(), issued.expiresAt()).getSeconds();

        log.info("Token emitido. username={}, expiresInSeconds={}", user.getUsername(), expiresIn);
        return new AuthenticationResult(issued.token(), "Bearer", Math.max(expiresIn, 0));
    }
}
