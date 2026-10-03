package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.AddressNormalizer;

/**
 * Validates that an owner payload carries an address in at least one accepted form: a structured
 * {@code addressLine1} that is non-blank after normalization, or the flat {@code address} that is
 * non-blank after normalization. A {@code null} payload passes. This keeps the request contract
 * backward-compatible — an owner supplying only the flat {@code address} is still accepted — while
 * allowing the structured form to stand on its own.
 */
public class AddressFormValidator implements ConstraintValidator<AddressFormValidation, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        return AddressNormalizer.normalize(owner.getAddressLine1()) != null
            || AddressNormalizer.normalize(owner.getAddress()) != null;
    }
}
