package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint asserting that an owner supplies a postal address in EITHER form:
 * a non-blank structured {@code addressLine1}, or the flat {@code address} (see
 * {@link AddressPresenceValidator}). It is a class-level constraint because the check spans
 * the two alternative address inputs.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AddressPresenceValidator.class)
@Documented
public @interface AddressPresenceValidation {

    String message() default "An address must be supplied as either 'addressLine1' or 'address'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
