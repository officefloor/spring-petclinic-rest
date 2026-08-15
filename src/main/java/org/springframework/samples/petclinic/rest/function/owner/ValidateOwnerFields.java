package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a body that is missing (absent) or blank
 * (whitespace only) in any of firstName, lastName, address, city or telephone — {@code @Size(min = 1)}
 * on the DTO does not catch a whitespace-only value, so the check is explicit here. The remaining
 * schema constraints (pattern, max length) are validated too, so a bad value is still a 400.
 *
 * <p>Binds the body directly (no {@code @Valid}) so that a missing field surfaces as this rule's
 * {@code errors} response rather than the generic schema-validation escalation, and publishes the
 * body for {@link BuildOwner} to consume.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Validator validator, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> errors = new ArrayList<>();
        normalizeAddress(request);
        addIfBlank(errors, "firstName", request.getFirstName());
        addIfBlank(errors, "lastName", request.getLastName());
        addIfBlank(errors, "address", request.getAddress());
        addIfBlank(errors, "city", request.getCity());
        normalizeTelephone(errors, request);
        for (ConstraintViolation<OwnerFieldsDto> violation : validator.validate(request)) {
            String field = violation.getPropertyPath().toString();
            if (!field.isEmpty() && !errors.contains(field)) {
                errors.add(field);
            }
        }
        if (!errors.isEmpty()) {
            throw new MissingOwnerFieldsException(errors);
        }
        normalizeEmail(request);
        validated.set(request);
    }

    /**
     * Email is optional. When absent nothing is done; when present the schema {@code @Email}
     * constraint (checked above) has already rejected a syntactically invalid address with a
     * {@code 400}, so a value reaching here is valid and is stored and returned lower-cased.
     */
    private static void normalizeEmail(OwnerFieldsDto request) {
        String email = request.getEmail();
        if (email != null) {
            request.setEmail(email.toLowerCase(java.util.Locale.ROOT));
        }
    }

    /**
     * Canonically normalize the address (trim, collapse whitespace, upper-case, expand common
     * abbreviations) and write it back onto the request so it is stored and returned in normalized
     * form. Running before the blank check means an address that is whitespace-only — and so blank
     * after normalization — is rejected as an {@code address} required-field error (400).
     */
    private static void normalizeAddress(OwnerFieldsDto request) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
    }

    private static void addIfBlank(List<String> errors, String name, String value) {
        if (value == null || value.isBlank()) {
            errors.add(name);
        }
    }

    /**
     * Normalize the telephone into E.164 form. Spaces, dashes and brackets are stripped. A leading
     * '+' with country code is kept as-is; otherwise the number is assumed to be Australian ('+61')
     * and a single leading '0' is dropped from the national digits. The result must have 8 to 15
     * digits after the '+'. The E.164 value is written back onto the request so it is stored and
     * returned as {@code telephone}. Anything that cannot form a valid E.164 number is a
     * {@code telephone} error (400).
     */
    private static void normalizeTelephone(List<String> errors, OwnerFieldsDto request) {
        String e164 = toE164(request.getTelephone());
        if (e164 == null) {
            errors.add("telephone");
            return;
        }
        request.setTelephone(e164);
    }

    /**
     * Convert a raw telephone into E.164 form, or return {@code null} when it cannot form a valid
     * E.164 number (fewer than 8 or more than 15 digits after the '+', or stray non-digit
     * characters remaining after the allowed separators are stripped).
     */
    static String toE164(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
