package ru.sejapoe.tinkab.dto.image;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

//@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
public record UploadImageResponse(
        @NotNull
        UUID imageId
) {

}
