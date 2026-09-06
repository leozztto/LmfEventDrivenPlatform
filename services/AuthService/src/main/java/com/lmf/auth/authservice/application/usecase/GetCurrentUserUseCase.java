package com.lmf.auth.authservice.application.usecase;

import com.lmf.auth.authservice.application.usecase.result.UserProfileResult;
import com.lmf.auth.authservice.domain.exception.UserNotFoundException;
import com.lmf.auth.authservice.domain.model.user.Role;
import com.lmf.auth.authservice.domain.model.user.User;
import com.lmf.auth.authservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResult execute(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        return new UserProfileResult(
                user.getId(),
                user.getUsername(),
                user.getEmail().value(),
                user.getRoles().stream().map(Role::name).sorted().toList(),
                user.isEnabled(),
                user.getCreatedAt());
    }
}
