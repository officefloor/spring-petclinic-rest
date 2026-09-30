package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a telephone number can be normalized into a valid E.164 number.
 * Non-digit separators (spaces, parentheses, dashes, ...) are ignored; a leading
 * {@code '+'} keeps the supplied country code, otherwise {@code +61} is assumed and a
 * single leading {@code '0'} is dropped. The result must carry 8 to 15 digits after the
 * {@code '+'}. This mirrors the create-time normalization performed by the mapper.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelephoneValidator.class)
@Documented
public @interface TelephoneValidation {

    String message() default "Telephone number must form a valid E.164 number (8 to 15 digits after the '+')";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
