package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.PostcodeRules;

/**
 * Validates an owner payload's postcode against its city, delegating to {@link PostcodeRules}, the
 * single source of truth for the fixed region ranges. A {@code null} payload or a postcode that is
 * absent, malformed (left to {@code @Pattern}) or valid for the city's region passes; only a
 * 4-digit postcode out of range for the city's known region is rejected.
 */
public class PostcodeValidator implements ConstraintValidator<PostcodeValidation, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        return PostcodeRules.isValidForCity(owner.getCity(), owner.getPostcode());
    }
}
