package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint enforcing the cross-field postcode rule: a supplied postcode must be valid
 * for the owner's city. The check needs both {@code city} and {@code postcode}, so it is applied to
 * the whole owner payload rather than a single field. A {@code null} postcode is accepted (the
 * field is optional) and 4-digit formatting is enforced separately by {@code @Pattern}; this
 * constraint only rejects a well-formed postcode that is out of range for the city's known region.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostcodeValidator.class)
@Documented
public @interface PostcodeValidation {

    String message() default "Postcode is not valid for the owner's city";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
