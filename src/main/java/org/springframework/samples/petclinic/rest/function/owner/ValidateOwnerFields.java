package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        List<String> errors = new ArrayList<>();
        checkPresent("firstName", request.getFirstName(), errors);
        checkPresent("lastName", request.getLastName(), errors);
        // Reject an address that is blank once normalized (trim/collapse/upper-case), so
        // whitespace-only input fails the required-field check like any other missing field.
        String address = AddressNormalizer.normalize(request.getAddress());
        if (address.isEmpty()) {
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
        // Store and return the normalized address; later steps read it back via @Val.
        request.setAddress(address);
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
            request.setEmail(email.toLowerCase());
        }
        validated.set(request);
    }

    private static void checkPresent(String field, String value, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(field);
        }
    }
}
