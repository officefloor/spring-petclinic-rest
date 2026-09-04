package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint for owner input: when a 'postcode' is present it must fall within the
 * inclusive range of the owner's city region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
 * A city with no known region, or an absent postcode, is accepted.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPostcodeValidator.class)
@Documented
public @interface ValidPostcode {

    String message() default "Postcode is not valid for the city's region";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
