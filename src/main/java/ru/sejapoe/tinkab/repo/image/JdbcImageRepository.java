package ru.sejapoe.tinkab.repo.image;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.sejapoe.tinkab.domain.ImageEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcImageRepository implements ImageRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(@NotNull ImageEntity imageEntity) {
        jdbcTemplate.update(
                "INSERT INTO images VALUES (?, ?, ?, ?)",
                imageEntity.id(),
                imageEntity.filename(),
                imageEntity.size(),
                imageEntity.userId()
        );
    }

    @Override
    public void remove(UUID uuid) {
        jdbcTemplate.update("DELETE FROM images WHERE id = ?", uuid);
    }

    @Override
    public Optional<ImageEntity> get(UUID uuid) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM images WHERE id = ?",
                    new DataClassRowMapper<>(ImageEntity.class),
                    uuid
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public List<ImageEntity> getByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM images WHERE user_id = ?",
                new DataClassRowMapper<>(ImageEntity.class),
                userId
        );
    }
}
