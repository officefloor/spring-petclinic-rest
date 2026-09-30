package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        // absence is handled by @NotNull / @NotEmpty; otherwise the value must be able
        // to form a valid E.164 number.
        return TelephoneNormalizer.isValid(telephone);
    }
}
