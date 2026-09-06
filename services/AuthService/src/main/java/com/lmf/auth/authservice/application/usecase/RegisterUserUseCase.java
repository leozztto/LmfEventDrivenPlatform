package com.lmf.auth.authservice.application.usecase;

import com.lmf.auth.authservice.application.usecase.command.RegisterUserCommand;
import com.lmf.auth.authservice.application.usecase.result.RegisterUserResult;
import com.lmf.auth.authservice.domain.exception.EmailAlreadyExistsException;
import com.lmf.auth.authservice.domain.exception.UsernameAlreadyExistsException;
import com.lmf.auth.authservice.domain.model.user.Email;
import com.lmf.auth.authservice.domain.model.user.Role;
import com.lmf.auth.authservice.domain.model.user.User;
import com.lmf.auth.authservice.domain.repository.UserRepository;
import com.lmf.auth.authservice.domain.service.PasswordHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional
    public RegisterUserResult execute(RegisterUserCommand command) {
        log.info("Registrando usuário. username={}", command.username());

        Email email = new Email(command.email());

        if (userRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException(command.username());
        }
        if (userRepository.existsByEmail(email.value())) {
            throw new EmailAlreadyExistsException(email.value());
        }

        String passwordHash = passwordHasher.hash(command.password());
        User user = new User(command.username(), email, passwordHash, Set.of(Role.ROLE_USER));
        User saved = userRepository.save(user);

        log.info("Usuário registrado. id={}, username={}", saved.getId(), saved.getUsername());
        return new RegisterUserResult(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail().value(),
                saved.getRoles().stream().map(Role::name).sorted().toList(),
                saved.getCreatedAt());
    }
}
