package ru.sejapoe.tinkab.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MediaTypeValidator implements ConstraintValidator<ru.sejapoe.tinkab.validation.annotations.MediaType, MultipartFile> {
    List<MediaType> mediaTypes;

    @Override
    public void initialize(ru.sejapoe.tinkab.validation.annotations.MediaType constraintAnnotation) {
        mediaTypes = MediaType.parseMediaTypes(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        String contentType = multipartFile.getContentType();
        if (contentType == null) {
            return false;
        }
        MediaType fileMediaType = MediaType.parseMediaType(contentType);
        return mediaTypes.stream().anyMatch(mediaType ->
                mediaType.includes(fileMediaType)
        );
    }
}
