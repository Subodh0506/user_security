package com.security.security.validation.validator;

import com.security.security.dto.user.CreateUserRequest;
import com.security.security.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordNotContainingUsernameValidator
        implements ConstraintValidator<ValidPassword, CreateUserRequest> {

    @Override
    public boolean isValid(CreateUserRequest user, ConstraintValidatorContext context) {
        return !user.getPassword().toLowerCase()
                .contains(user.getName().toLowerCase());
    }
}
