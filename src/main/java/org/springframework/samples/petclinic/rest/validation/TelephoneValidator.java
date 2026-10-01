package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Validates that a telephone number can be converted to a canonical E.164 form.
 * The check delegates to {@link Owner#normalizeTelephone(String)}, the single
 * source of truth for how a telephone is normalized before it is persisted, so
 * the validated input and the stored value agree on validity.
 */
public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        return Owner.normalizeTelephone(telephone) != null;
    }
}
