package ru.sejapoe.tinkab.dto.image;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ApplyImageFiltersResponse(
        @NotNull UUID requestId
) {
}
