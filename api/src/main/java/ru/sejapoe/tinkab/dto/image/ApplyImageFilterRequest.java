package ru.sejapoe.tinkab.dto.image;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.sejapoe.tinkab.domain.ImageFilter;

import java.util.List;

public record ApplyImageFilterRequest(
        @NotEmpty List<Filter> filters
) {
    public record Filter(@NotNull ImageFilter type, @Nullable Object params) {
    }
}
