package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.util.AddressNormalizer;

/**
 * Validates that an address is not blank once normalized. The check delegates to
 * {@link AddressNormalizer}, the same normalization applied by {@code OwnerMapper}, so a value that
 * passes this constraint is exactly one that normalizes to the canonical form that gets stored. A
 * {@code null} value is left to {@code @NotBlank}.
 */
public class AddressValidator implements ConstraintValidator<AddressValidation, String> {

    @Override
    public boolean isValid(String address, ConstraintValidatorContext context) {
        if (address == null) {
            return true;
        }
        return AddressNormalizer.normalize(address) != null;
    }
}
