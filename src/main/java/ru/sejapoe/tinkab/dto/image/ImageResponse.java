package ru.sejapoe.tinkab.dto.image;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ImageResponse(
        @NotNull
        String filename,
        @NotNull
        UUID imageId,
        @NotNull
        Integer size
) {
}
