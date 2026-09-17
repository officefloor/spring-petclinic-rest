package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint requiring an owner to supply a postal address in either form: a non-blank
 * structured {@code addressLine1} or the flat {@code address}. Keeping the check at class level (see
 * {@link AddressFormValidator}) preserves backward compatibility — an owner with only the flat
 * {@code address} stays valid — while also accepting a structured address that omits the flat field.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AddressFormValidator.class)
@Documented
public @interface ValidAddress {

    String message() default "An address is required, as either addressLine1 or address";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
