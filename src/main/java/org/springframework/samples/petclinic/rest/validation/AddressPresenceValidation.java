package org.springframework.samples.petclinic.rest.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Class-level constraint on owner fields: an owner must supply an address in either
 * form — a non-blank structured {@code addressLine1}, or the flat {@code address}.
 * This keeps earlier flat-address requests accepted while allowing the structured form.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AddressPresenceValidator.class)
@Documented
public @interface AddressPresenceValidation {

    String message() default "Either addressLine1 or address must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

/** Accepts an owner that carries a non-blank {@code addressLine1} or a non-blank {@code address}. */
class AddressPresenceValidator implements ConstraintValidator<AddressPresenceValidation, OwnerFieldsDto> {

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        return hasText(owner.getAddressLine1()) || hasText(owner.getAddress());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
