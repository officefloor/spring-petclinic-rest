package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint asserting that an owner's optional postcode is valid for its city.
 * It is a cross-field check (the postcode is validated against the region derived from the city),
 * so it is applied to the whole owner payload rather than a single field. The actual rule is
 * owned by {@link org.springframework.samples.petclinic.model.Owner#isPostcodeValidForCity(String, String)}.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostcodeValidator.class)
@Documented
public @interface PostcodeValidation {

    String message() default "Postcode must be 4 digits and valid for the owner's city";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
