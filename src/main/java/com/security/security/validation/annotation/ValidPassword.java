package com.security.security.validation.annotation;
import com.security.security.validation.validator.PasswordNotContainingUsernameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordNotContainingUsernameValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password should not contain username";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}