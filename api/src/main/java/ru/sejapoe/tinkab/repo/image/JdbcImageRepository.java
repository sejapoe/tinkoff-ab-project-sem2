package ru.sejapoe.tinkab.repo.image;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.sejapoe.tinkab.domain.ImageEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcImageRepository implements ImageRepository {
    private final JdbcClient jdbcClient;

    @Override
    public void save(@NotNull ImageEntity imageEntity) {
        jdbcClient.sql("INSERT INTO images VALUES (?, ?, ?, ?)")
                .params(imageEntity.id())
                .param(imageEntity.filename())
                .param(imageEntity.size())
                .param(imageEntity.userId())
                .update();
    }

    @Override
    public void remove(UUID uuid) {
        jdbcClient.sql("DELETE FROM images WHERE id = ?")
                .param(uuid)
                .update();
    }

    @Override
    public Optional<ImageEntity> get(UUID uuid) {
        return jdbcClient.sql("SELECT * FROM images WHERE id = ?")
                .param(uuid)
                .query(ImageEntity.class)
                .optional();
    }

    @Override
    public List<ImageEntity> getByUserId(Long userId) {
        return jdbcClient.sql("SELECT * FROM images WHERE user_id = ?")
                .param(userId)
                .query(ImageEntity.class)
                .list();
    }
}
