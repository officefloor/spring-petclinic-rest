package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.Localities;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, so an
 * incomplete body is a 400 whose {@code errors} array names each offending field. Also
 * normalizes the telephone to E.164 form (see {@link TelephoneE164}), republishing the
 * normalized value so it is stored and returned as {@code telephone}; a telephone that
 * cannot form a valid E.164 number is a 400 too.
 * The optional {@code email} is validated only when present: a syntactically invalid
 * address is a 400, otherwise it is normalized to lower-case and republished so it is
 * stored and returned lower-cased.
 * The {@code address} is normalized (see {@link AddressNormalizer}) and republished so it
 * is stored and returned in canonical form; an address that is blank after normalization
 * is a required-field 400.
 * Runs first and republishes the body as a variable for {@link BuildOwner}.
 */
public class ValidateOwnerFields {

    /** Syntactic email check: one '@', non-empty local part, and a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** A well-formed postcode is exactly four digits. */
    private static final Pattern POSTCODE = Pattern.compile("^[0-9]{4}$");

    /** Region -> inclusive 4-digit postcode range {low, high}. A city whose region is not
     *  listed (derives "UNKNOWN") accepts any well-formed 4-digit postcode. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingFieldsException {
        List<String> errors = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            errors.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            errors.add("lastName");
        }
        String address = AddressNormalizer.normalize(request.getAddress());
        if (address.isEmpty()) {
            errors.add("address");
        }
        else {
            request.setAddress(address);
        }
        if (isBlank(request.getCity())) {
            errors.add("city");
        }
        String telephone = TelephoneE164.normalize(request.getTelephone());
        if (telephone == null) {
            errors.add("telephone");
        }
        else {
            request.setTelephone(telephone);
        }
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase();
            if (!EMAIL.matcher(normalized).matches()) {
                errors.add("email");
            }
            else {
                request.setEmail(normalized);
            }
        }
        // Postcode is optional; validated only when present. A malformed (non-4-digit) postcode
        // is a 400, and a well-formed postcode outside its city's region range is a 400 too.
        // A city with no known region accepts any 4-digit postcode.
        String postcode = request.getPostcode();
        if (postcode != null && !postcode.isBlank()) {
            String trimmed = postcode.trim();
            if (!POSTCODE.matcher(trimmed).matches() || !inRegionRange(request.getCity(), trimmed)) {
                errors.add("postcode");
            }
            else {
                request.setPostcode(trimmed);
            }
        }
        if (!errors.isEmpty()) {
            throw new MissingFieldsException(errors);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** True when the 4-digit {@code postcode} is acceptable for the city's region: either the
     *  city has no known region (accepts any 4-digit postcode) or the postcode falls within the
     *  region's inclusive range. */
    private static boolean inRegionRange(String city, String postcode) {
        int[] range = REGION_POSTCODES.get(Localities.region(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
