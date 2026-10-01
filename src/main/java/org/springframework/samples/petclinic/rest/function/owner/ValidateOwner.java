package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.Regions;
import org.springframework.samples.petclinic.model.OwnerTelephone;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInvalidException;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsMissingException;
import org.springframework.samples.petclinic.rest.escalation.OwnerPostcodeInvalidException;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners} and the single step that binds the request body. Rejects a
 * body that is missing or blank in any required field (firstName, lastName, address, city, telephone)
 * with a 400 whose {@code errors} array lists the offending field names. A whitespace-only value is
 * treated as blank — the DTO's {@code @Size(min = 1)} alone would accept it — so this check, not bean
 * validation, is the authoritative implementation of the required-field rule.
 *
 * <p>The address is normalized (see {@link OwnerAddress}) before the required-field check, so an
 * address that is blank once whitespace is collapsed is rejected as missing. The normalized value is
 * written back onto the body so the normalized, abbreviation-expanded address is what gets stored,
 * returned and compared for household matching.
 *
 * <p>The telephone is then converted to E.164 form (see {@link OwnerTelephone#toE164(String)}); a
 * value that cannot form a valid E.164 number is rejected with a 400 via
 * {@link OwnerTelephoneInvalidException}. The E.164 value is written back onto the body so it is what
 * gets stored and returned.
 *
 * <p>Email is optional. When absent (null) it is left untouched. When present it must be a
 * syntactically valid address; otherwise the create is rejected with a 400 via
 * {@link OwnerEmailInvalidException}. A valid email is lower-cased and written back onto the body so
 * the lower-cased value is what gets stored and returned. The validated body is republished for
 * {@link BuildOwner}.
 */
public class ValidateOwner {

    /** Pragmatic syntactic check: non-empty local part, '@', non-empty domain containing a dot. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** A postcode is a string of exactly four digits. */
    private static final Pattern POSTCODE = Pattern.compile("^[0-9]{4}$");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsMissingException, OwnerTelephoneInvalidException,
            OwnerEmailInvalidException, OwnerPostcodeInvalidException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        String address = OwnerAddress.normalize(request.getAddress());
        if (address.isEmpty()) {
            missing.add("address");
        }
        if (isBlank(request.getCity())) {
            missing.add("city");
        }
        if (isBlank(request.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new OwnerFieldsMissingException(missing);
        }
        request.setAddress(address);
        String e164 = OwnerTelephone.toE164(request.getTelephone());
        if (e164 == null) {
            throw new OwnerTelephoneInvalidException(request.getTelephone());
        }
        request.setTelephone(e164);

        String email = request.getEmail();
        if (email != null) {
            if (!EMAIL.matcher(email).matches()) {
                throw new OwnerEmailInvalidException(email);
            }
            request.setEmail(email.toLowerCase());
        }

        // Postcode is optional. When present it must be a 4-digit value that is valid for the
        // owner's city region; a city with no known region accepts any 4-digit value.
        String postcode = request.getPostcode();
        if (postcode != null) {
            if (!POSTCODE.matcher(postcode).matches()
                    || !Regions.postcodeMatchesCity(request.getCity(), postcode)) {
                throw new OwnerPostcodeInvalidException(postcode);
            }
        }

        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
