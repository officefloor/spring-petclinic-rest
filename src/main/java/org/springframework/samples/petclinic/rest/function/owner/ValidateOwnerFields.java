package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone. Throwing lists every offending field, so the
 * client sees them all at once. The address is normalized (trim/collapse whitespace, upper-case, and
 * expand ST/RD/AVE) and written back, and is treated as absent when blank after normalization. It
 * also normalizes the telephone into E.164 form (keeping a leading
 * '+' and country code when present, otherwise assuming '+61' and dropping a single leading '0'),
 * rejecting with 400 when it cannot form a valid E.164 number; the normalized value is
 * written back onto the body. When an optional {@code email} is present it must be a syntactically
 * valid address (else 400); it is lower-cased and written back. A supplied {@code registrationDate}
 * later than the server's current date is rejected with 400 (an absent date is accepted and defaults
 * to today when the owner is built). On success it publishes the body for {@link BuildOwner} to map.
 */
public class ValidateOwnerFields {

    /** Syntactic email check: a non-empty local part, an {@code @}, then a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    /** Disposable email domains that are rejected with 400. */
    private static final Set<String> DISPOSABLE_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        List<String> errors = new ArrayList<>();
        checkPresent("firstName", request.getFirstName(), errors);
        checkPresent("lastName", request.getLastName(), errors);
        // The address may be supplied in structured form (a non-blank addressLine1 plus an optional
        // addressLine2) or as the flat 'address' input (kept for backward compatibility). Normalize
        // whichever fields are supplied (trim/collapse whitespace, upper-case, expand ST/RD/AVE) and
        // treat a value that is blank after normalization as absent. The owner is valid when it
        // supplies an address in EITHER form.
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flatAddress = AddressNormalizer.normalize(request.getAddress());
        boolean hasStructured = !line1.isEmpty();
        boolean hasFlat = !flatAddress.isEmpty();
        if (!hasStructured && !hasFlat) {
            errors.add("address");
        }
        checkPresent("city", request.getCity(), errors);
        checkPresent("telephone", request.getTelephone(), errors);
        // A supplied registrationDate may not be in the future: reject a date later than the
        // server's current date. Absent is accepted (it defaults to today when the owner is built).
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            errors.add("registrationDate");
        }
        if (!errors.isEmpty()) {
            throw new OwnerFieldsInvalidException(errors);
        }
        // Store and return the normalized address; later steps read it back via @Val. Prefer the
        // structured fields when present: the composed 'address' is the normalized addressLine1, with
        // a single space and the normalized addressLine2 appended when addressLine2 is present.
        // Otherwise fall back to the normalized flat address.
        if (hasStructured) {
            request.setAddressLine1(line1);
            request.setAddressLine2(line2.isEmpty() ? null : line2);
            request.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
        }
        else {
            request.setAddressLine1(null);
            request.setAddressLine2(null);
            request.setAddress(flatAddress);
        }
        String telephone = E164Telephone.normalize(request.getTelephone());
        if (telephone == null) {
            throw new OwnerFieldsInvalidException(List.of("telephone"));
        }
        request.setTelephone(telephone);
        String email = request.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim();
            if (!EMAIL.matcher(email).matches()) {
                throw new OwnerFieldsInvalidException(List.of("email"));
            }
            email = email.toLowerCase();
            // Reject disposable-domain addresses (the domain part after the final '@').
            String domain = email.substring(email.lastIndexOf('@') + 1);
            if (DISPOSABLE_DOMAINS.contains(domain)) {
                throw new OwnerFieldsInvalidException(List.of("email"));
            }
            request.setEmail(email);
        }
        validated.set(request);
    }

    private static void checkPresent(String field, String value, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(field);
        }
    }
}
