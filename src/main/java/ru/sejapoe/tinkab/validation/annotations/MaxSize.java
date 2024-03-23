package ru.sejapoe.tinkab.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.sejapoe.tinkab.validation.MaxSizeValidator;

import java.lang.annotation.*;

/**
 * Annotation to specify the maximum size constraint for a field.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxSizeValidator.class)
@Documented
public @interface MaxSize {
    /**
     * Specifies the maximum size allowed for the field in a format like "32G", "10M".
     */
    String value();

    String message() default "File size is bigger than allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
