package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that an email address does not use a disposable-email domain (see
 * {@link DisposableEmailRule}). An absent email is accepted.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DisposableEmailValidator.class)
@Documented
public @interface NotDisposableEmail {

    String message() default "Email must not use a disposable-email domain";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
