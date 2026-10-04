package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that a telephone number can be converted to E.164 form, mirroring the
 * normalization the {@code OwnerMapper} performs on create. The validator does not
 * mutate the value, it only checks that a valid E.164 number can be formed from it
 * (see {@link E164Telephone}).
 */
public class TelephoneValidator implements ConstraintValidator<TelephoneValidation, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        if (telephone == null) {
            return true; // null is handled by the field's @NotNull constraint
        }
        return E164Telephone.isValid(telephone);
    }
}
