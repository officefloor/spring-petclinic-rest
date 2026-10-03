package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.util.TelephoneNormalizer;

/**
 * Validates that a telephone number can be normalized to a valid E.164 number (a leading
 * {@code '+'} followed by 8 to 15 digits). The check delegates to {@link TelephoneNormalizer},
 * the same normalization applied by {@code OwnerMapper}, so a value that passes this constraint
 * is exactly one that normalizes to the E.164 form that gets stored. A {@code null} value is
 * left to {@code @NotBlank}.
 */
public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        if (telephone == null) {
            return true;
        }
        return TelephoneNormalizer.toE164(telephone) != null;
    }
}
