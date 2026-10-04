package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Validates that an owner supplies a postal address in EITHER form: a non-blank structured
 * {@code addressLine1}, or the flat {@code address}. This keeps the request contract
 * backward-compatible (earlier owners carrying only the flat {@code address} stay valid) while
 * also accepting owners that provide the structured address fields instead.
 */
public class AddressPresenceValidator implements ConstraintValidator<AddressPresenceValidation, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        return isPresent(owner.getAddressLine1()) || isPresent(owner.getAddress());
    }

    private boolean isPresent(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
