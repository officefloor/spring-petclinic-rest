package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates an owner's optional postcode against its city's region: a 4-digit
 * postcode must fall in the region's inclusive range (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any
 * 4-digit postcode. An absent or non-4-digit value passes here - absence is
 * allowed and the 4-digit format is enforced separately by the field pattern.
 */
public class PostcodeValidator implements ConstraintValidator<PostcodeValidation, OwnerFieldsDto> {

    private static final Map<String, int[]> CITY_RANGE = Map.of(
        "Sydney", new int[] {2000, 2099},
        "Melbourne", new int[] {3000, 3099},
        "Brisbane", new int[] {4000, 4099});

    @Override
    public boolean isValid(OwnerFieldsDto owner, ConstraintValidatorContext context) {
        String postcode = owner.getPostcode();
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return true;
        }
        String city = owner.getCity();
        int[] range = city == null ? null : CITY_RANGE.get(city);
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
