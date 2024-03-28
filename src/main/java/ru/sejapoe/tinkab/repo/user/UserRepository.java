package ru.sejapoe.tinkab.repo.user;

import ru.sejapoe.tinkab.domain.UserEntity;

import java.util.Optional;

public interface UserRepository {
    UserEntity add(String username, String password);

    Optional<UserEntity> findByUsername(String username);
}
