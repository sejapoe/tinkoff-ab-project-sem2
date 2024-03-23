package ru.sejapoe.tinkab.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import ru.sejapoe.tinkab.utils.SizeConverter;
import ru.sejapoe.tinkab.validation.annotations.MaxSize;

public class MaxSizeValidator implements ConstraintValidator<MaxSize, MultipartFile> {
    private long maxSize;

    @Override
    public void initialize(MaxSize constraintAnnotation) {
        maxSize = SizeConverter.convertToBytes(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        return multipartFile.getSize() <= maxSize;
    }
}
