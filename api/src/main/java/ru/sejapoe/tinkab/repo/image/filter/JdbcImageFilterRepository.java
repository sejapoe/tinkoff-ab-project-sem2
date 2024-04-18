package ru.sejapoe.tinkab.repo.image.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.sejapoe.tinkab.domain.ImageFilterEntity;
import ru.sejapoe.tinkab.domain.ImageRequestStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcImageFilterRepository implements ImageFilterRepository {
    private final JdbcClient jdbcClient;

    @Override
    public ImageFilterEntity create(UUID originalImageId) {
        return jdbcClient
                .sql("INSERT INTO image_requests (original_image_id) VALUES (:originalImageId) RETURNING *")
                .param("originalImageId", originalImageId)
                .query(ImageFilterEntity.class)
                .single();
    }

    @Override
    public ImageFilterEntity update(UUID uuid, ImageRequestStatus status, UUID editedImageId) {
        return jdbcClient
                .sql("UPDATE image_requests SET status=:status, edited_image_id=:editedId WHERE id=:id RETURNING *")
                .param("id", uuid)
                .param("status", status.name())
                .param("editedId", editedImageId)
                .query(ImageFilterEntity.class)
                .single();
    }

    @Override
    public Optional<ImageFilterEntity> get(UUID uuid) {
        return jdbcClient.sql("SELECT * FROM image_requests WHERE id=:id")
                .param("id", uuid)
                .query(ImageFilterEntity.class)
                .optional();
    }
}
