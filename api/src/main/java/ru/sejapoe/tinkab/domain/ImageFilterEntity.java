package ru.sejapoe.tinkab.domain;

import java.util.UUID;

public record ImageFilterEntity(
        UUID id,
        UUID originalImageId,
        UUID editedImageId,
        ImageRequestStatus status
) {
}
