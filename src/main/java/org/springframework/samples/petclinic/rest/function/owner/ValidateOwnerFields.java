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
 * Rejects a create request that is missing or blank in any required owner field, before
 * {@link BuildOwner} runs. Reads the body once and republishes it for later steps.
 */
public class ValidateOwnerFields {

    /** A syntactically valid address: local-part@domain with a dotted domain, no spaces. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** Disposable email domains rejected outright: an email in one of these is a 400. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        List<String> errors = new ArrayList<>();
        checkRequired("firstName", request.getFirstName(), errors);
        checkRequired("lastName", request.getLastName(), errors);
        checkRequired("city", request.getCity(), errors);
        checkRequired("telephone", request.getTelephone(), errors);
        normalizeAddress(request, errors);
        normalizeTelephone(request, errors);
        normalizeEmail(request, errors);
        validatePostcode(request, errors);
        validateRegistrationDate(request, errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsInvalidException(errors);
        }
        validated.set(request);
    }

    private static void checkRequired(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }

    /**
     * Normalizes the supplied address, preferring the structured
     * {@code addressLine1}/{@code addressLine2} pair over the flat {@code address} (see
     * {@link AddressFields}), and stores the normalized parts and composed canonical
     * address back on the request so later steps persist, compare and return them. An
     * address is required in one form or the other: a request that supplies neither a
     * non-blank {@code addressLine1} nor a non-blank flat {@code address} is a 400 (adds an
     * "address" error) and nothing is stored back.
     */
    private static void normalizeAddress(OwnerFieldsDto request, List<String> errors) {
        if (!AddressFields.normalizeInto(request)) {
            errors.add("address");
        }
    }

    /**
     * Normalizes the telephone to E.164 form (see {@link TelephoneE164}), storing the
     * result back on the request so later steps persist and return it. A value that
     * cannot form a valid E.164 number is a 400 (adds a "telephone" error).
     */
    private static void normalizeTelephone(OwnerFieldsDto request, List<String> errors) {
        String telephone = request.getTelephone();
        if (telephone == null) {
            return;
        }
        String e164 = TelephoneE164.toE164(telephone);
        if (e164 != null) {
            request.setTelephone(e164);
        }
        else if (!errors.contains("telephone")) {
            errors.add("telephone");
        }
    }

    /**
     * Email is optional. When present (non-blank) it must be a syntactically valid address
     * whose domain is not on the disposable-domain blocklist; an invalid or disposable value
     * is a 400 (adds an "email" error). A valid value is stored lower-cased back on the request
     * so later steps persist and return it lower-cased.
     */
    private static void normalizeEmail(OwnerFieldsDto request, List<String> errors) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String trimmed = email.trim();
        if (EMAIL.matcher(trimmed).matches() && !isDisposableDomain(trimmed)) {
            request.setEmail(trimmed.toLowerCase());
        }
        else if (!errors.contains("email")) {
            errors.add("email");
        }
    }

    /** True when the email's domain (case-insensitive) is on the disposable-domain blocklist. */
    private static boolean isDisposableDomain(String email) {
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        return DISPOSABLE_DOMAINS.contains(email.substring(at + 1).toLowerCase());
    }

    /**
     * Postcode is optional. When present (non-blank) it must be a 4-digit value that is valid
     * for the owner's city per the fixed region ranges (see
     * {@link org.springframework.samples.petclinic.model.Postcode}); an out-of-range or
     * malformed value is a 400 (adds a "postcode" error). A valid value is stored trimmed back
     * on the request so later steps persist and return it. A city with no known region accepts
     * any 4-digit postcode, keeping the create request backward-compatible when absent.
     */
    /**
     * Registration date is optional (it defaults to the server's current date in
     * {@link BuildOwner} when absent). When supplied it must not be later than the server's
     * current date; a future value is a 400 (adds a "registrationDate" error).
     */
    private static void validateRegistrationDate(OwnerFieldsDto request, List<String> errors) {
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            errors.add("registrationDate");
        }
    }

    private static void validatePostcode(OwnerFieldsDto request, List<String> errors) {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String trimmed = postcode.trim();
        if (org.springframework.samples.petclinic.model.Postcode.isValidForCity(trimmed, request.getCity())) {
            request.setPostcode(trimmed);
        }
        else if (!errors.contains("postcode")) {
            errors.add("postcode");
        }
    }
}
