package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint asserting that an owner's postcode, when present, is valid for
 * the region derived from its city (see {@link PostcodeValidator}). It is a class-level
 * constraint because the check spans two fields (postcode and city).
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostcodeValidator.class)
@Documented
public @interface PostcodeValidation {

    String message() default "Postcode is not valid for the city's region";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
