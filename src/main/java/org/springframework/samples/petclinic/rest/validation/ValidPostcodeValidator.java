package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a supplied postcode that is out of range for its city's region. Region ranges are fixed;
 * an unknown region, or a blank/malformed postcode (left to the field-level pattern), is accepted.
 */
public class ValidPostcodeValidator implements ConstraintValidator<ValidPostcode, OwnerFieldsDto> {

    /** Region to its inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        String postcode = owner == null ? null : owner.getPostcode();
        if (postcode == null || postcode.isBlank() || owner.getCity() == null) {
            return true;
        }
        int[] range = REGION_RANGE.get(Locality.of(owner.getCity()));
        if (range == null || !postcode.matches("\\d{4}")) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
