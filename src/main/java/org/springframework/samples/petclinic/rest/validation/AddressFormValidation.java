package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint enforcing that an owner supplies an address in EITHER form: a non-blank
 * structured {@code addressLine1} or the flat {@code address}. The check needs both fields, so it is
 * applied to the whole owner payload rather than a single field. The flat {@code address} remains
 * accepted for backward compatibility, so an owner that supplies only it (as earlier minimal owners
 * did) still validates; a non-blank {@code addressLine1} alone is likewise accepted. Blankness is
 * decided by {@link org.springframework.samples.petclinic.util.AddressNormalizer}, the same
 * normalization applied elsewhere.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AddressFormValidator.class)
@Documented
public @interface AddressFormValidation {

    String message() default "An owner must supply an address: either a non-blank addressLine1 or the flat address";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
