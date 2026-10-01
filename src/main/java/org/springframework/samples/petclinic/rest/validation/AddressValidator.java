package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Validates that an owner supplies a postal address in at least one accepted form. The check
 * delegates to {@link Owner#hasAddress(String, String)}, the single source of truth, so an owner
 * with a non-blank structured {@code addressLine1} or a non-blank flat {@code address} is accepted
 * while one with neither is rejected. On failure the violation is reported against the
 * {@code addressLine1} field so the error response names a concrete field.
 */
public class AddressValidator implements ConstraintValidator<AddressValidation, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        if (Owner.hasAddress(owner.getAddressLine1(), owner.getAddress())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode("addressLine1")
            .addConstraintViolation();
        return false;
    }
}
