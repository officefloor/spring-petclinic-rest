package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Validates an owner's postcode against its city using {@link PostcodeRule}. On failure the
 * violation is reported against the {@code postcode} property.
 */
public class PostcodeValidator implements ConstraintValidator<ValidPostcode, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null || PostcodeRule.isValid(owner.getCity(), owner.getPostcode())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode("postcode")
            .addConstraintViolation();
        return false;
    }
}
