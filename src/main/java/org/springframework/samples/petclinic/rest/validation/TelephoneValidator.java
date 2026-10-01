package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that a telephone number contains exactly 10 digits once every
 * non-digit character has been stripped. The stripping here mirrors the
 * normalization applied by the mapper when the value is persisted, so the
 * validated input and the stored value agree on the digit count.
 */
public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        if (telephone == null) {
            return false;
        }
        return telephone.replaceAll("\\D", "").length() == 10;
    }
}
