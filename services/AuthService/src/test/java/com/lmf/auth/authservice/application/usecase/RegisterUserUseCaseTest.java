package com.lmf.auth.authservice.application.usecase;

import com.lmf.auth.authservice.application.usecase.command.RegisterUserCommand;
import com.lmf.auth.authservice.application.usecase.result.RegisterUserResult;
import com.lmf.auth.authservice.domain.exception.EmailAlreadyExistsException;
import com.lmf.auth.authservice.domain.exception.UsernameAlreadyExistsException;
import com.lmf.auth.authservice.domain.model.user.User;
import com.lmf.auth.authservice.domain.repository.UserRepository;
import com.lmf.auth.authservice.domain.service.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private RegisterUserUseCase useCase;

    private final RegisterUserCommand command =
            new RegisterUserCommand("alice", "Alice@Example.com", "s3cret123");

    @Test
    void gravaUsuarioComHashEPapelUser() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordHasher.hash("s3cret123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserResult result = useCase.execute(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("HASHED");
        assertThat(result.roles()).containsExactly("ROLE_USER");
        assertThat(result.email()).isEqualTo("alice@example.com");
    }

    @Test
    void falhaQuandoUsernameJaExiste() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UsernameAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void falhaQuandoEmailJaExiste() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }
}
