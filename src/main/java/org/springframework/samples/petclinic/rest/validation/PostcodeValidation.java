package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostcodeValidator.class)
@Documented
public @interface PostcodeValidation {

    String message() default "Postcode is not valid for the city's region";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
