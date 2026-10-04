package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.Postcodes;

/**
 * Validates that an owner's postcode, when present, is valid for the region derived from its
 * city (see {@link Postcodes}). The 4-digit format is enforced separately by the field's pattern
 * constraint, so a missing or non-4-digit postcode is accepted here and left for that constraint.
 */
public class PostcodeValidator implements ConstraintValidator<PostcodeValidation, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        return Postcodes.isValidForCity(owner.getPostcode(), owner.getCity());
    }
}
