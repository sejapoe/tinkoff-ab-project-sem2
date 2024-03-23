package ru.sejapoe.tinkab.dto.image;

import org.springframework.web.multipart.MultipartFile;
import ru.sejapoe.tinkab.validation.annotations.MaxSize;
import ru.sejapoe.tinkab.validation.annotations.MediaType;

public record UploadImageRequest(
        @MaxSize("10M")
        @MediaType("image/jpeg,image/png")
        MultipartFile file
) {
}
