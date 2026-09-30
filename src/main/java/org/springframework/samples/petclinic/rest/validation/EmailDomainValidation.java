package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that an email address does not use a disposable-email domain. The domain
 * (the part after the final {@code '@'}) is compared case-insensitively against a fixed
 * blocklist ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}); a
 * value using one of those domains is rejected. Absence and general syntactic validity
 * are handled separately, so a {@code null} or blank value passes this constraint.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailDomainValidator.class)
@Documented
public @interface EmailDomainValidation {

    String message() default "Email address must not use a disposable-email domain "
        + "(mailinator.com, tempmail.com, guerrillamail.com)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
