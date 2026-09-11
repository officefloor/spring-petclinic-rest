package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
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
     * Normalizes the address (see {@link AddressNormalizer}) and stores the result back on
     * the request so later steps persist, compare and return the canonical form. The
     * address is required: a value that is blank after normalization is a 400 (adds an
     * "address" error) and is not stored back.
     */
    private static void normalizeAddress(OwnerFieldsDto request, List<String> errors) {
        String normalized = AddressNormalizer.normalize(request.getAddress());
        if (normalized.isEmpty()) {
            errors.add("address");
        }
        else {
            request.setAddress(normalized);
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
     * Email is optional. When present (non-blank) it must be a syntactically valid address;
     * an invalid value is a 400 (adds an "email" error). A valid value is stored lower-cased
     * back on the request so later steps persist and return it lower-cased.
     */
    private static void normalizeEmail(OwnerFieldsDto request, List<String> errors) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String trimmed = email.trim();
        if (EMAIL.matcher(trimmed).matches()) {
            request.setEmail(trimmed.toLowerCase());
        }
        else if (!errors.contains("email")) {
            errors.add("email");
        }
    }

    /**
     * Postcode is optional. When present (non-blank) it must be a 4-digit value that is valid
     * for the owner's city per the fixed region ranges (see
     * {@link org.springframework.samples.petclinic.model.Postcode}); an out-of-range or
     * malformed value is a 400 (adds a "postcode" error). A valid value is stored trimmed back
     * on the request so later steps persist and return it. A city with no known region accepts
     * any 4-digit postcode, keeping the create request backward-compatible when absent.
     */
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
