package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that an owner supplies an address in at least one accepted form: either a non-blank
 * structured {@code addressLine1} or the flat {@code address}. This keeps the request contract
 * backward-compatible (a flat address alone is still accepted) while allowing the structured form
 * to stand on its own. The {@code city} remains independently required.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidAddressFormValidator.class)
@Documented
public @interface ValidAddressForm {

    String message() default "An address is required: supply either 'addressLine1' or 'address'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
