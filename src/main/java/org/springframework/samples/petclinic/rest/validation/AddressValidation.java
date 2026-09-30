package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that an owner supplies a postal address in at least one form: a non-blank
 * structured {@code addressLine1}, or the flat {@code address}. The structured
 * {@code addressLine1}/{@code addressLine2} fields are preferred when present, while the
 * flat {@code address} input remains accepted for backward compatibility, so earlier
 * minimal owners (flat {@code address} only) are still valid. The {@code city} requirement
 * is enforced separately by that field's own rules.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AddressValidator.class)
@Documented
public @interface AddressValidation {

    String message() default "An address is required: supply a non-blank 'addressLine1' or the flat 'address'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
