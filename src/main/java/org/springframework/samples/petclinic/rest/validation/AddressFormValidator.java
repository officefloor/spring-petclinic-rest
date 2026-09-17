package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Validates that an owner supplies an address in either form: a non-blank structured
 * {@code addressLine1} or the flat {@code address}. On failure the violation is reported against the
 * {@code addressLine1} property.
 */
public class AddressFormValidator implements ConstraintValidator<ValidAddress, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null || hasText(owner.getAddressLine1()) || hasText(owner.getAddress())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode("addressLine1")
            .addConstraintViolation();
        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
