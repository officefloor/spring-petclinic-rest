package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Validates that an owner's optional postcode is valid for its city. The check delegates to
 * {@link Owner#isPostcodeValidForCity(String, String)}, the single source of truth for postcode
 * validity, so an absent postcode is accepted while a present one must be four digits and within
 * the range of the city's region. On failure the violation is reported against the {@code postcode}
 * field so the error response names it.
 */
public class PostcodeValidator implements ConstraintValidator<PostcodeValidation, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        if (Owner.isPostcodeValidForCity(owner.getPostcode(), owner.getCity())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode("postcode")
            .addConstraintViolation();
        return false;
    }
}
