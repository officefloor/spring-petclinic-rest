package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a telephone number can be normalized to E.164 form (see
 * {@link TelephoneNormalizer#toE164(String)}): a leading {@code '+'} with 8 to 15 digits.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelephoneValidator.class)
@Documented
public @interface Telephone {

    String message() default "Telephone must be a valid E.164 number (+ and 8 to 15 digits)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
