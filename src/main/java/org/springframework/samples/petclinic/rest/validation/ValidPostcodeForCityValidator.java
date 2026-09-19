package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.PostcodePolicy;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Enforces {@link ValidPostcodeForCity}: a supplied 4-digit postcode must be valid for the
 * owner's city. An absent postcode is accepted (optional when absent), and a malformed
 * postcode is left to the field-level format constraint rather than reported here.
 */
public class ValidPostcodeForCityValidator implements ConstraintValidator<ValidPostcodeForCity, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        String postcode = owner.getPostcode();
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return true;
        }
        if (PostcodePolicy.isValidForCity(owner.getCity(), postcode)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode("postcode")
            .addConstraintViolation();
        return false;
    }
}
