package com.lmf.auth.authservice.application.usecase;

import com.lmf.auth.authservice.application.port.TokenIssuer;
import com.lmf.auth.authservice.application.usecase.command.AuthenticateUserCommand;
import com.lmf.auth.authservice.application.usecase.result.AuthenticationResult;
import com.lmf.auth.authservice.domain.exception.DisabledUserException;
import com.lmf.auth.authservice.domain.exception.InvalidCredentialsException;
import com.lmf.auth.authservice.domain.model.user.Email;
import com.lmf.auth.authservice.domain.model.user.Role;
import com.lmf.auth.authservice.domain.model.user.User;
import com.lmf.auth.authservice.domain.repository.UserRepository;
import com.lmf.auth.authservice.domain.service.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenIssuer tokenIssuer;

    @InjectMocks
    private AuthenticateUserUseCase useCase;

    private final AuthenticateUserCommand command = new AuthenticateUserCommand("alice", "s3cret123");

    private User enabledUser() {
        return new User("alice", new Email("alice@example.com"), "HASH", Set.of(Role.ROLE_USER));
    }

    @Test
    void emiteTokenNoCaminhoFeliz() {
        User user = enabledUser();
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("s3cret123", "HASH")).thenReturn(true);
        when(tokenIssuer.issue(user)).thenReturn(
                new TokenIssuer.IssuedToken("token-123", Instant.now(), Instant.now().plusSeconds(3600)));

        AuthenticationResult result = useCase.execute(command);

        assertThat(result.accessToken()).isEqualTo("token-123");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresInSeconds()).isBetween(3500L, 3600L);
    }

    @Test
    void falhaQuandoUsuarioNaoExiste() {
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(InvalidCredentialsException.class);
        verify(tokenIssuer, never()).issue(any());
    }

    @Test
    void falhaQuandoSenhaIncorreta() {
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(enabledUser()));
        when(passwordHasher.matches("s3cret123", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(InvalidCredentialsException.class);
        verify(tokenIssuer, never()).issue(any());
    }

    @Test
    void falhaQuandoUsuarioDesabilitado() {
        User disabled = new User(UUID.randomUUID(), "alice", new Email("alice@example.com"), "HASH",
                Set.of(Role.ROLE_USER), false, OffsetDateTime.now());
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(disabled));

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(DisabledUserException.class);
        verify(tokenIssuer, never()).issue(any());
    }
}
