package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.Regions;
import org.springframework.samples.petclinic.model.DisposableDomains;
import org.springframework.samples.petclinic.model.OwnerTelephone;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailDisposableException;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInvalidException;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsMissingException;
import org.springframework.samples.petclinic.rest.escalation.OwnerPostcodeInvalidException;
import org.springframework.samples.petclinic.rest.escalation.OwnerRegistrationDateFutureException;
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
 * {@link OwnerEmailInvalidException}. A syntactically valid email whose domain is on the
 * disposable-domain blocklist is rejected with a 400 via {@link OwnerEmailDisposableException}. A
 * valid, accepted email is lower-cased and written back onto the body so the lower-cased value is
 * what gets stored and returned. The validated body is republished for {@link BuildOwner}.
 */
public class ValidateOwner {

    /** Pragmatic syntactic check: non-empty local part, '@', non-empty domain containing a dot. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** A postcode is a string of exactly four digits. */
    private static final Pattern POSTCODE = Pattern.compile("^[0-9]{4}$");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsMissingException, OwnerTelephoneInvalidException,
            OwnerEmailInvalidException, OwnerEmailDisposableException,
            OwnerPostcodeInvalidException, OwnerRegistrationDateFutureException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        // Address may be supplied in structured form (addressLine1 [+ addressLine2]) or as the flat
        // 'address' field. Structured fields take precedence when present. Normalization (see
        // OwnerAddress) applies to whichever fields are supplied; the composed, normalized value is
        // written back onto the flat 'address' so it is what gets stored, returned and read by
        // everything downstream.
        String addressLine1 = OwnerAddress.normalize(request.getAddressLine1());
        String addressLine2 = OwnerAddress.normalize(request.getAddressLine2());
        boolean structured = !addressLine1.isEmpty();
        String address = structured
                ? OwnerAddress.compose(addressLine1, addressLine2)
                : OwnerAddress.normalize(request.getAddress());
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
        if (structured) {
            request.setAddressLine1(addressLine1);
            request.setAddressLine2(addressLine2.isEmpty() ? null : addressLine2);
        }
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
            String domain = DisposableDomains.domainOf(email);
            if (DisposableDomains.isBlocked(domain)) {
                throw new OwnerEmailDisposableException(domain);
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

        // Registration may be supplied but never dated in the future: a value later than the
        // server's current date is rejected with a 400.
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new OwnerRegistrationDateFutureException(registrationDate);
        }

        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
