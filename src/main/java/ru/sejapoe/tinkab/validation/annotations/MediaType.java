package ru.sejapoe.tinkab.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.sejapoe.tinkab.validation.MediaTypeValidator;

import java.lang.annotation.*;

/**
 * Annotation to specify acceptable media types of file
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MediaTypeValidator.class)
@Documented
public @interface MediaType {
    /**
     * Specifies the acceptable media types, multiple media types should be provided by comma.
     * e.g. "image/jpeg,image/png" or "image/*"
     */
    String value();

    String message() default "Not acceptable file's content type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
