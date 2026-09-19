package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a telephone value can be converted to canonical E.164 form (a leading
 * {@code '+'} followed by 8 to 15 digits), assuming the {@code '+61'} country code when the
 * value carries no explicit one.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelephoneDigitsValidator.class)
@Documented
public @interface TelephoneDigits {

    String message() default "Telephone must be a valid E.164 number (8 to 15 digits after the country code)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
