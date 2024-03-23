package ru.sejapoe.tinkab.dto.image;

import java.util.List;

public record ListImagesResponse(
        List<ImageResponse> images
) {
}
