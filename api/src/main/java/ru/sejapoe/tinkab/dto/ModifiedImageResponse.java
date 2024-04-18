package ru.sejapoe.tinkab.dto;

import jakarta.validation.constraints.NotNull;
import ru.sejapoe.tinkab.domain.ImageRequestStatus;

import java.util.UUID;

public record ModifiedImageResponse(
        @NotNull UUID imageId,
        @NotNull ImageRequestStatus status
) {
}
