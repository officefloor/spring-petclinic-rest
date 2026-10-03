package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that a telephone number contains exactly 10 digits once every non-digit
 * character has been stripped out. The stripping mirrors the normalization applied by
 * {@code OwnerMapper}, so a value that passes this constraint is one that normalizes to
 * the 10-digit form that gets stored. A {@code null} value is left to {@code @NotBlank}.
 */
public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        if (telephone == null) {
            return true;
        }
        return telephone.replaceAll("\\D", "").length() == 10;
    }
}
