package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * address is a 400, an otherwise-valid address whose domain is on the disposable-domain
 * blocklist is a 400, otherwise it is normalized to lower-case and republished so it is
 * stored and returned lower-cased.
 * The address may be supplied in a structured form ({@code addressLine1} plus an optional
 * {@code addressLine2}) or as the flat {@code address}; the structured form is preferred when a
 * non-blank {@code addressLine1} is present. Whichever fields are supplied are normalized (see
 * {@link AddressNormalizer}) and republished, and the stored/returned {@code address} is the
 * composed value (normalized {@code addressLine1}, plus a space and the normalized
 * {@code addressLine2} when present) falling back to the normalized flat {@code address}. An owner
 * that supplies an address in neither form is a required-field 400.
 * The optional {@code registrationDate} is validated only when present: a date later than
 * the server's current date is a 400.
 * Runs first and republishes the body as a variable for {@link BuildOwner}.
 */
public class ValidateOwnerFields {

    /** Syntactic email check: one '@', non-empty local part, and a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Disposable email domains that are rejected: a syntactically valid address whose domain
     *  (lower-cased) is listed here is a 400. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

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
        applyAddress(request, errors);
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
            else if (DISPOSABLE_DOMAINS.contains(normalized.substring(normalized.indexOf('@') + 1))) {
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
        // registrationDate is optional; validated only when present. A date later than the
        // server's current date is a 400 — an owner cannot be registered in the future.
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            errors.add("registrationDate");
        }
        if (!errors.isEmpty()) {
            throw new MissingFieldsException(errors);
        }
        validated.set(request);
    }

    /**
     * Normalizes whichever address fields are supplied and republishes them, preferring the
     * structured form. A non-blank {@code addressLine1} (with an optional {@code addressLine2})
     * wins over the flat {@code address}; either form satisfies the address requirement. The
     * stored/returned {@code address} is the composed value: the normalized {@code addressLine1},
     * plus a single space and the normalized {@code addressLine2} when present, falling back to the
     * normalized flat {@code address}. An owner supplying neither is a required-field 400.
     */
    private static void applyAddress(OwnerFieldsDto request, List<String> errors) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flat = AddressNormalizer.normalize(request.getAddress());
        if (!line1.isEmpty()) {
            request.setAddressLine1(line1);
            String composed = line1;
            if (!line2.isEmpty()) {
                request.setAddressLine2(line2);
                composed = line1 + " " + line2;
            }
            else {
                request.setAddressLine2(null);
            }
            request.setAddress(composed);
        }
        else if (!flat.isEmpty()) {
            request.setAddress(flat);
        }
        else {
            errors.add("address");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** True when the 4-digit {@code postcode} is acceptable for the city's region: either the
     *  city has no known region (accepts any 4-digit postcode) or the postcode falls within the
     *  region's inclusive range. */
    private static boolean inRegionRange(String city, String postcode) {
        if (isBlank(city)) {
            return true; // no city yet: no region constraint (the missing city is flagged separately)
        }
        int[] range = REGION_POSTCODES.get(Localities.region(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
