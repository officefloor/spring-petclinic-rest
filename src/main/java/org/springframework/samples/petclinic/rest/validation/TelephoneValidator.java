package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that a telephone number contains exactly 10 digits once every
 * non-digit character has been stripped. This mirrors the normalization the
 * {@code OwnerMapper} performs on create: the validator does not mutate the
 * value, it only checks that the normalized form is exactly 10 digits.
 */
public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        if (telephone == null) {
            return true; // null is handled by the field's @NotNull constraint
        }
        return telephone.replaceAll("\\D", "").length() == 10;
    }
}
