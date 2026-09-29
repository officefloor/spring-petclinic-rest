package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a telephone number can be normalized into E.164 form, i.e. a
 * leading '+' followed by 8 to 15 digits (see {@link TelephoneValidator}).
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelephoneValidator.class)
@Documented
public @interface TelephoneValidation {

    String message() default "Telephone number must be a valid E.164 number (8 to 15 digits)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
