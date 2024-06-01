package ru.sejapoe.tinkab.worker.tagging.dto;

import java.util.List;

public record ImaggaTagsResponseDto(
        Result result,
        ImaggaStatus status
) {
    public record Result(
            List<Tag> tags
    ) {
    }

    public record Tag(
            int confidence,
            TagName tag
    ) {
    }

    public record TagName(
            String en
    ) {
    }
}
