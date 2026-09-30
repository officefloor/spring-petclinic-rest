package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {

        if (telephone == null) {
            return true; // absence is handled by @NotNull / @NotEmpty
        }

        return telephone.replaceAll("\\D", "").length() == 10;
    }
}
