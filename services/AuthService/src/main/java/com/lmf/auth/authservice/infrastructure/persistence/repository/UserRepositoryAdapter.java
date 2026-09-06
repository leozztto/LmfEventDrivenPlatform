package com.lmf.auth.authservice.infrastructure.persistence.repository;

import com.lmf.auth.authservice.domain.model.user.User;
import com.lmf.auth.authservice.domain.repository.UserRepository;
import com.lmf.auth.authservice.infrastructure.persistence.mapper.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public User save(User user) {
        var entity = userEntityMapper.toEntity(user);
        var saved = springDataUserRepository.save(entity);
        return userEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username).map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        String normalized = usernameOrEmail == null ? null : usernameOrEmail.trim();
        return springDataUserRepository
                .findByUsernameOrEmail(normalized, normalized == null ? null : normalized.toLowerCase())
                .map(userEntityMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }
}
