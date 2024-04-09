package ru.sejapoe.tinkab.repo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.sejapoe.tinkab.domain.UserEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {
    private final JdbcClient jdbcClient;

    @Override
    public UserEntity add(String username, String password) {
        return jdbcClient.sql("INSERT INTO users VALUES (default, :username, :password) RETURNING *")
                .param("username", username)
                .param("password", password)
                .query(UserEntity.class)
                .single();
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return jdbcClient.sql("SELECT * FROM users WHERE username = ?")
                .param(username)
                .query(UserEntity.class)
                .optional();
    }
}
