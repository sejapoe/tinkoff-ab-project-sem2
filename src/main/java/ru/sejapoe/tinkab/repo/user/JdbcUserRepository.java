package ru.sejapoe.tinkab.repo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.sejapoe.tinkab.domain.UserEntity;

@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserEntity add(String username, String password) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO users VALUES (default, ?, ?) RETURNING *",
                new DataClassRowMapper<>(UserEntity.class),
                username,
                password
        );
    }

    @Override
    public UserEntity findByUsername(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE username = ?",
                new DataClassRowMapper<>(UserEntity.class),
                username
        );
    }
}
