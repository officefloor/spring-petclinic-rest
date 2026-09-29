package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.util.E164Telephone;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Rejects a telephone number that cannot be normalized into E.164 form (see
 * {@link E164Telephone}). The normalization mirrors what is applied before an
 * {@code Owner} is persisted, so the value the client is told about matches the
 * value that gets stored.
 */
public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        return E164Telephone.toE164(telephone) != null;
    }
}
