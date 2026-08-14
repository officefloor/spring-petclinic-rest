package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsValidationException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request that is missing or blank in any required field
 * (firstName, lastName, address, city, telephone) before {@link BuildOwner} runs.
 *
 * <p>Binds the body here (the single {@code @RequestBody} for the pipeline) and republishes it
 * so later steps read it via {@code @Val}. On failure it throws
 * {@link OwnerFieldsValidationException}, which the escalation handler turns into a 400 whose
 * {@code errors} array lists each offending field.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsValidationException {
        List<String> errors = new ArrayList<>();
        requireText("firstName", request.getFirstName(), errors);
        requireText("lastName", request.getLastName(), errors);
        normalizeAddress(request, errors);
        requireText("city", request.getCity(), errors);
        requireText("telephone", request.getTelephone(), errors);
        normalizeTelephone(request, errors);
        OwnerEmail.normalize(request, errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsValidationException(errors);
        }
        validated.set(request);
    }

    /**
     * Normalizes the address to its canonical form (see {@link AddressNormalizer}) and stores it back
     * on the request so later steps persist and return it, and so household comparisons use it. The
     * required-field check happens here on the normalized value: an address that is blank after
     * normalization adds an {@code address} error (400).
     */
    private static void normalizeAddress(OwnerFieldsDto request, List<String> errors) {
        String normalized = AddressNormalizer.normalize(request.getAddress());
        if (normalized.isEmpty()) {
            errors.add("address");
            return;
        }
        request.setAddress(normalized);
    }

    /**
     * Normalizes the telephone to E.164 and stores it back on the request so later steps persist
     * and return it. A leading {@code +} and its country code are kept; otherwise country code
     * {@code +61} is assumed and a single leading {@code 0} is dropped from the national digits.
     * Spaces, dashes and brackets are stripped, and the result must carry 8 to 15 digits after the
     * {@code +}. So {@code "0412 345 678"} becomes {@code "+61412345678"}. A number that cannot form
     * valid E.164 adds a {@code telephone} error (400). Skipped when the telephone is missing/blank,
     * which {@link #requireText} has already flagged.
     */
    private static void normalizeTelephone(OwnerFieldsDto request, List<String> errors) {
        String telephone = request.getTelephone();
        if (telephone == null || telephone.isBlank()) {
            return;
        }
        String e164 = toE164(telephone);
        if (e164 == null) {
            errors.add("telephone");
            return;
        }
        request.setTelephone(e164);
    }

    /**
     * Returns the E.164 form of {@code telephone}, or {@code null} when it cannot form a valid one.
     */
    private static String toE164(String telephone) {
        String input = telephone.trim();
        boolean international = input.startsWith("+");
        // strip spaces, dashes and brackets from the remainder
        String cleaned = (international ? input.substring(1) : input).replaceAll("[\\s\\-()]", "");
        if (!cleaned.matches("\\d+")) {
            return null; // stray characters left over — not a phone number
        }
        String digits;
        if (international) {
            digits = cleaned;
        }
        else {
            // no country code: assume +61 and drop a single leading national '0'
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        return "+" + digits;
    }

    private static void requireText(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }
}
