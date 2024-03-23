package ru.sejapoe.tinkab.dto.image;

import java.util.UUID;

public record ImageResponse(
        String filename,
        UUID imageId,
        Long size
) {
}
