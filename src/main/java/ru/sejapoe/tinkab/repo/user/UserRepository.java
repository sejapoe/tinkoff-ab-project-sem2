package ru.sejapoe.tinkab.repo.user;

import ru.sejapoe.tinkab.domain.UserEntity;

public interface UserRepository {
    UserEntity add(String username, String password);

    UserEntity findByUsername(String username);
}
