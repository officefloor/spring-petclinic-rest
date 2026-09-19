package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that an owner's postcode, when present, is valid for the owner's city per the
 * fixed region ranges (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known
 * region accepts any 4-digit postcode. An absent postcode is always accepted.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPostcodeForCityValidator.class)
@Documented
public @interface ValidPostcodeForCity {

    String message() default "Postcode is not valid for the city's region";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
