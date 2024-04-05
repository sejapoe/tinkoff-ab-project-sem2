package ru.sejapoe.tinkab.dto;

import jakarta.validation.constraints.NotNull;

public record SuccessResponse(
        @NotNull
        boolean success,
        String message
) {
}
