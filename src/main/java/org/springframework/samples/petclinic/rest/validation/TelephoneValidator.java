package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Rejects a telephone number that does not contain exactly ten digits after
 * every non-digit character has been removed. The stripping mirrors the
 * normalization applied before an {@code Owner} is persisted, so the value the
 * client is told about matches the value that gets stored.
 */
public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        return digits.length() == 10;
    }
}
