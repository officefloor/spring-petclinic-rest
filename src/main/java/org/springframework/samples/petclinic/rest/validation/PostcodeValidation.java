package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that an owner's postcode, WHEN PRESENT, is valid for its city's region. The
 * region is derived from a fixed city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC,
 * Brisbane-&gt;QLD) and each region has an inclusive 4-digit range (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit
 * postcode. An absent postcode is always accepted, keeping the request backward-compatible;
 * the 4-digit format itself is enforced separately by the field's pattern.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostcodeValidator.class)
@Documented
public @interface PostcodeValidation {

    String message() default "Postcode must be within its city's region range "
        + "(NSW 2000-2099, VIC 3000-3099, QLD 4000-4099)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
