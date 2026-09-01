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
 * Class-level rule that an owner supplies an address in either form: a non-blank
 * structured {@code addressLine1}, or the flat backward-compatible {@code address}.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AddressRequired.Validator.class)
@Documented
public @interface AddressRequired {

    String message() default "An address is required (addressLine1 or address)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<AddressRequired, OwnerFieldsDto> {

        @Override
        public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
            return present(owner.getAddressLine1()) || present(owner.getAddress());
        }

        private static boolean present(String value) {
            return value != null && !value.isBlank();
        }
    }
}
