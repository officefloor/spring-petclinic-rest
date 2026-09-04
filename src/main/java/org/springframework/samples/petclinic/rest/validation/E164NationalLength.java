package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that an E.164 telephone carries the national-number length its country code requires
 * (e.g. '+61' => 9 national digits, '+1' => 10). Country codes with no known rule are accepted.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = E164NationalLengthValidator.class)
@Documented
public @interface E164NationalLength {

    String message() default "Telephone national-number length is wrong for its country code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
