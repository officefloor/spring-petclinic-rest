package org.springframework.samples.petclinic.rest.function.owner;

import java.lang.reflect.Method;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.core.MethodParameter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Validates the optional {@code postcode} against the owner's city.
 *
 * <p>Postcode is validated <strong>only when present</strong> and is optional when absent, so an owner
 * created without a postcode is still accepted. When supplied it must be four digits (enforced by the
 * schema pattern and re-checked defensively here) and, when the city maps to a known region, must fall
 * within that region's inclusive range per the fixed table:
 * NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. A city with no known region accepts any 4-digit
 * postcode. An out-of-range postcode is rejected with 400 via {@link MethodArgumentNotValidException}.
 */
public class ValidatePostcode {

    private static final Method SERVICE_METHOD;

    static {
        try {
            SERVICE_METHOD = ValidatePostcode.class.getMethod("service", OwnerFieldsDto.class);
        }
        catch (NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /** Fixed city-to-region table, mirroring the read-time locality derivation. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region to its inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws MethodArgumentNotValidException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent — nothing to validate
        }
        BindingResult binding = new BeanPropertyBindingResult(request, "ownerFieldsDto");
        if (!postcode.matches("[0-9]{4}")) {
            binding.rejectValue("postcode", "Pattern", "must be a 4-digit postcode");
        }
        else {
            String region = CITY_REGION.get(request.getCity());
            int[] range = region == null ? null : REGION_POSTCODES.get(region);
            if (range != null) {
                int value = Integer.parseInt(postcode);
                if (value < range[0] || value > range[1]) {
                    binding.rejectValue("postcode", "PostcodeOutOfRange",
                        "postcode " + postcode + " is not valid for region " + region);
                }
            }
        }
        if (binding.hasErrors()) {
            throw new MethodArgumentNotValidException(new MethodParameter(SERVICE_METHOD, 0), binding);
        }
    }
}
