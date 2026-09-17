package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint validating an owner's {@code postcode} against its {@code city}: when a
 * postcode is present it must be four digits and, for a city with a known region, fall within that
 * region's fixed range (see {@link PostcodeRule}). An absent postcode is always accepted.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostcodeValidator.class)
@Documented
public @interface ValidPostcode {

    String message() default "Postcode must be 4 digits and valid for the city's region";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
