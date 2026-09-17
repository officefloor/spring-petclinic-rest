package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DisposableEmailValidator implements ConstraintValidator<NotDisposableEmail, String> {

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return !DisposableEmailRule.isDisposable(email);
    }
}
