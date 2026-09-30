package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Cross-field validator for {@link PostcodeValidation}: checks a supplied postcode against
 * the range of its city's region. Absence of a postcode, or a postcode for a city whose
 * region is unknown, is always accepted. The 4-digit format is enforced by the field's own
 * pattern, so a malformed postcode is left for that rule to reject.
 */
public class PostcodeValidator implements ConstraintValidator<PostcodeValidation, OwnerFieldsDto> {

    /** Fixed city-to-region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region to inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        if (owner == null) {
            return true;
        }
        String postcode = owner.getPostcode();
        // Optional when absent; the 4-digit format is enforced by the field pattern, so
        // anything not a plain 4-digit value is left for that rule to reject.
        if (postcode == null || !postcode.matches("^[0-9]{4}$")) {
            return true;
        }
        String region = CITY_REGION.get(owner.getCity());
        int[] range = region == null ? null : REGION_POSTCODES.get(region);
        if (range == null) {
            // City with no known region accepts any 4-digit postcode.
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
