package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Cross-field validator for {@link AddressValidation}: an owner is valid only when it
 * supplies an address in at least one form, either a non-blank structured
 * {@code addressLine1} or the flat {@code address}. Both being absent (or blank) is
 * rejected. The flat form staying accepted keeps the request backward-compatible.
 */
public class AddressValidator implements ConstraintValidator<AddressValidation, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        boolean hasStructured = owner.getAddressLine1() != null && !owner.getAddressLine1().isBlank();
        boolean hasFlat = owner.getAddress() != null && !owner.getAddress().isBlank();
        return hasStructured || hasFlat;
    }
}
