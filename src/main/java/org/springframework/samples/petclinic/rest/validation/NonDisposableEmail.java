package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Rejects an email whose domain is on the disposable-domain blocklist
 * (mailinator.com, tempmail.com, guerrillamail.com). A blank or absent email is accepted.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NonDisposableEmailValidator.class)
@Documented
public @interface NonDisposableEmail {

    String message() default "Email domain is not allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
