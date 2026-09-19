package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Enforces {@link ValidAddressForm}: the owner must carry an address in at least one form, i.e. a
 * non-blank {@code addressLine1} or a non-blank flat {@code address}. When neither is present the
 * violation is reported against {@code addressLine1}.
 */
public class ValidAddressFormValidator implements ConstraintValidator<ValidAddressForm, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        if (hasText(owner.getAddressLine1()) || hasText(owner.getAddress())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode("addressLine1")
            .addConstraintViolation();
        return false;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
