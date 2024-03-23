package ru.sejapoe.tinkab.repo.image;

import ru.sejapoe.tinkab.domain.ImageEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository {
    /**
     * Saves entity to database
     *
     * @param imageEntity entity to save
     */
    void save(ImageEntity imageEntity);

    /**
     * Removes entity from database
     *
     * @param uuid UUID of entity to remove
     */
    void remove(UUID uuid);

    /**
     * Get entity by UUID
     *
     * @param uuid UUID of entity to get
     * @return optional with entity if present or empty if not
     */
    Optional<ImageEntity> get(UUID uuid);

    /**
     * Get all entities with user id
     *
     * @param userId id for query
     * @return list of images
     */
    List<ImageEntity> getByUserId(Long userId);
}
