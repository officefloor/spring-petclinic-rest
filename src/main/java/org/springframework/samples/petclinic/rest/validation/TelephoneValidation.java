package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelephoneValidator.class)
@Documented
public @interface TelephoneValidation {

    String message() default "Telephone must be convertible to E.164 form (8 to 15 digits)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
