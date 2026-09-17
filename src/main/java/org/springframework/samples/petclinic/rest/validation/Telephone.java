package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a telephone number can be normalized to E.164 form (see
 * {@link TelephoneNormalizer#toE164(String)}): a leading {@code '+'} with 8 to 15 digits, whose
 * national-number length also matches its country code (see
 * {@link E164NationalNumberRule#hasValidNationalLength(String)}, e.g. {@code +61} requires 9
 * national digits and {@code +1} requires 10).
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
