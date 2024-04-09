package ru.sejapoe.tinkab.dto.image;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ListImagesResponse(
        @NotNull
        List<ImageResponse> images
) {
}
