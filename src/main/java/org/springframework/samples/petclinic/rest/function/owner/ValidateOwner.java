package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Binds the owner body and rejects it when any required field is missing or blank, so an
 * invalid body is a 400 (listing the offending field names) even when the owner does not exist.
 * Also normalizes the telephone to E.164 form (see {@link TelephoneE164}), publishing the
 * normalized value so it is stored and returned as {@code telephone}.
 * The address is normalized (see {@link AddressNormalizer}) before the required-field check, so a
 * value that is blank only after normalization is rejected, and the normalized form is stored and
 * returned as {@code address}.
 * The optional email is accepted when absent; when present it must be a syntactically valid
 * address (else 400) whose domain is not on the disposable-domain blocklist (else 400), and is
 * lower-cased so it is stored and returned as {@code email}.
 * Runs before {@link LoadOwner}/{@link BuildOwner} and publishes the body for later steps.
 */
public class ValidateOwner {

    /** Syntactic email check: a non-empty local part, an '@', then a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    /** A valid postcode is exactly four digits. */
    private static final Pattern POSTCODE = Pattern.compile("^[0-9]{4}$");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException,
            DisposableEmailDomainException, InvalidPostcodeException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        // Normalize whichever address fields are supplied (see AddressNormalizer). The structured
        // form (addressLine1 + optional addressLine2) is preferred; the flat 'address' input remains
        // accepted for backward compatibility. Normalizing before the required-field check means a
        // value that is blank only after normalization is rejected. An owner is valid when it
        // supplies an address in EITHER form: a non-blank addressLine1, or the flat address.
        String addressLine1 = AddressNormalizer.normalize(request.getAddressLine1());
        String addressLine2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flatAddress = AddressNormalizer.normalize(request.getAddress());
        request.setAddressLine1(addressLine1.isEmpty() ? null : addressLine1);
        request.setAddressLine2(addressLine2.isEmpty() ? null : addressLine2);
        // The stored/returned 'address' is the composed structured address when addressLine1 is
        // present (normalized addressLine1, plus a single space and addressLine2 when present),
        // otherwise the normalized flat address.
        String address = !addressLine1.isEmpty()
                ? AddressNormalizer.compose(addressLine1, addressLine2)
                : flatAddress;
        request.setAddress(address);
        if (isBlank(address)) {
            missing.add("address");
        }
        if (isBlank(request.getCity())) {
            missing.add("city");
        }
        if (isBlank(request.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        // Normalize telephone to E.164; reject anything that cannot form a valid number.
        String e164 = TelephoneE164.normalize(request.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(e164);
        // Email is optional. When present it must be syntactically valid; store it lower-cased.
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase();
            if (!EMAIL.matcher(normalized).matches()) {
                throw new InvalidEmailException(email);
            }
            // Reject syntactically valid addresses whose domain is a known disposable provider.
            String domain = normalized.substring(normalized.indexOf('@') + 1);
            if (DisposableEmail.isBlocked(domain)) {
                throw new DisposableEmailDomainException(email);
            }
            request.setEmail(normalized);
        }
        // Postcode is optional. When present it must be four digits and, when the city maps to a
        // known region, fall within that region's range; a city with no known region accepts any
        // 4-digit postcode. The (trimmed) value is stored and returned as given.
        String postcode = request.getPostcode();
        if (postcode != null && !postcode.isBlank()) {
            String trimmed = postcode.trim();
            if (!POSTCODE.matcher(trimmed).matches()) {
                throw new InvalidPostcodeException(postcode);
            }
            int[] range = CityRegion.postcodeRangeOf(request.getCity());
            if (range != null) {
                int value = Integer.parseInt(trimmed);
                if (value < range[0] || value > range[1]) {
                    throw new InvalidPostcodeException(postcode);
                }
            }
            request.setPostcode(trimmed);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
