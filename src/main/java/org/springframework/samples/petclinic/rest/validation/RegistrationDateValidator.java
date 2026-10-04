package org.springframework.samples.petclinic.rest.validation;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegistrationDateValidator implements ConstraintValidator<RegistrationDateValidation, LocalDate> {

    @Override
    public boolean isValid(LocalDate registrationDate, ConstraintValidatorContext context) {

        if (registrationDate == null) {
            return true;
        }

        if (registrationDate.isAfter(LocalDate.now())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Registration date cannot be in the future"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
