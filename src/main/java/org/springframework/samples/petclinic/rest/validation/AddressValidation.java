package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint asserting that an owner supplies a postal address in at least one of the
 * two accepted forms: a non-blank structured {@code addressLine1}, or the flat {@code address} kept
 * for backward compatibility. It is a cross-field check (either source field satisfies it), so it is
 * applied to the whole owner payload rather than a single field. The actual rule is owned by
 * {@link org.springframework.samples.petclinic.model.Owner#hasAddress(String, String)}.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AddressValidator.class)
@Documented
public @interface AddressValidation {

    String message() default "An address must be supplied, either as addressLine1 or as address";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
