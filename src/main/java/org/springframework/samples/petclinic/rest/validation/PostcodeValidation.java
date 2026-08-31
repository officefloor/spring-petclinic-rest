package org.springframework.samples.petclinic.rest.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.Localities;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Class-level constraint on owner fields: when a 4-digit {@code postcode} is present it must
 * fall in the inclusive range for the city's region ({@link Localities#regionOf}). A city with
 * no known region, or an absent postcode, is accepted.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostcodeRegionValidator.class)
@Documented
public @interface PostcodeValidation {

    String message() default "Postcode is not valid for the city's region";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

/** Checks a supplied 4-digit postcode against its city region's inclusive range. */
class PostcodeRegionValidator implements ConstraintValidator<PostcodeValidation, OwnerFieldsDto> {

    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] { 2000, 2099 },
        "VIC", new int[] { 3000, 3099 },
        "QLD", new int[] { 4000, 4099 });

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        String postcode = owner.getPostcode();
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return true;
        }
        int[] range = REGION_RANGE.get(Localities.regionOf(owner.getCity()));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
