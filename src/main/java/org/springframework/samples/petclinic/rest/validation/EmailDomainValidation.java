package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailDomainValidator.class)
@Documented
public @interface EmailDomainValidation {

    String message() default "Email domain is on the disposable-domain blocklist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
