package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a create whose firstName, lastName,
 * address, city or telephone is missing or blank before {@link BuildOwner} maps the body,
 * so an incomplete request is a 400 listing each field rather than a persisted owner. The
 * address may be supplied in structured form (a non-blank {@code addressLine1}, an optional
 * {@code addressLine2}) or as the flat {@code address}; the structured form is preferred and the
 * canonical {@code address} is composed from it (normalized addressLine1, plus a single space and
 * the normalized addressLine2 when present). An owner is valid when it supplies an address in
 * either form, plus a city.
 * Also normalizes the telephone to E.164 form (see {@link TelephoneE164}), storing the E.164
 * value on the body and rejecting a number that cannot form a valid E.164 string. An optional
 * email, when present, must be a syntactically valid address and is stored lower-cased.
 * Publishes the validated body for the later steps.
 */
public class RequireOwnerFields {

    /** Pragmatic syntactic check: one or more non-space/@ characters, an '@', a domain label,
     *  a dot, and a top-level label. Rejects inputs such as "not-an-email". */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** Disposable/throwaway email domains a create-owner request may not use. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException,
            DisposableEmailException {
        List<String> missing = new ArrayList<>();
        // Normalize whichever address fields are supplied, then settle on ONE canonical address.
        // The structured form (a non-blank addressLine1) is preferred; the flat 'address' input
        // remains accepted for backward compatibility. The composed 'address' is the normalized
        // addressLine1 with the normalized addressLine2 appended after a single space when an
        // addressLine2 is present. Doing this up-front means the blank check below rejects an
        // address supplied in neither form, and every later step that reads getAddress() sees the
        // one canonical value (structured when present, flat otherwise).
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flat = AddressNormalizer.normalize(request.getAddress());
        if (!line1.isEmpty()) {
            request.setAddressLine1(line1);
            if (line2.isEmpty()) {
                request.setAddressLine2(null);
                request.setAddress(line1);
            }
            else {
                request.setAddressLine2(line2);
                request.setAddress(line1 + " " + line2);
            }
        }
        else {
            request.setAddressLine1(null);
            request.setAddressLine2(null);
            request.setAddress(flat);
        }
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(request.getAddress())) {
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
        request.setTelephone(TelephoneE164.normalize(request.getTelephone()));
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase();
            if (!EMAIL.matcher(normalized).matches()) {
                throw new InvalidEmailException(email);
            }
            String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
            if (DISPOSABLE_DOMAINS.contains(domain)) {
                throw new DisposableEmailException(email);
            }
            request.setEmail(normalized);
        }
        else {
            request.setEmail(null);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
