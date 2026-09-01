package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DisposableEmailValidator.class)
@Documented
public @interface DisposableEmailValidation {

    String message() default "Email domain is not allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
