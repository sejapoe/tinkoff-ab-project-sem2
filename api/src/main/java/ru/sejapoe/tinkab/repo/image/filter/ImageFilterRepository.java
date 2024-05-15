package ru.sejapoe.tinkab.repo.image.filter;

import ru.sejapoe.tinkab.domain.ImageFilterEntity;
import ru.sejapoe.tinkab.domain.ImageRequestStatus;

import java.util.Optional;
import java.util.UUID;

public interface ImageFilterRepository {
    /**
     * Creates entity in database
     *
     * @param originalImageId UUID of original image
     */
    ImageFilterEntity create(UUID originalImageId);

    /**
     * Set Status for existing entity
     *
     * @param uuid          UUID of entity to remove
     * @param status        status to set
     * @param editedImageId id of edited image
     */
    ImageFilterEntity update(UUID uuid, ImageRequestStatus status, UUID editedImageId);

    /**
     * Get entity by UUID
     *
     * @param uuid UUID of entity to get
     * @return optional with entity if present or empty if not
     */
    Optional<ImageFilterEntity> get(UUID uuid);
}
