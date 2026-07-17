package org.generation.italy.fantafootball.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "must be at least 12 characters and include lowercase, uppercase, digit, and symbol characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
