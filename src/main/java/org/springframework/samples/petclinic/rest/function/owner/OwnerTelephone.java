package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInvalidException;

/**
 * Shared telephone handling for owner endpoints. A telephone is stored and returned in E.164 form:
 * keep a leading {@code '+'} and country code when the input supplies one, otherwise assume country
 * code {@code +61} and drop a single leading {@code '0'} from the national digits. Spaces, dashes and
 * brackets are stripped; the result must carry 8 to 15 digits after the {@code '+'}. So
 * {@code '0412 345 678'} becomes {@code '+61412345678'}.
 */
final class OwnerTelephone {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    private OwnerTelephone() {
    }

    /**
     * Convert a raw telephone into its E.164 representation.
     *
     * @throws OwnerTelephoneInvalidException when the input cannot form a valid E.164 number.
     */
    static String toE164(String raw) throws OwnerTelephoneInvalidException {
        if (raw == null) {
            throw new OwnerTelephoneInvalidException(raw);
        }
        String trimmed = raw.trim();
        boolean hasPlus = trimmed.startsWith("+");
        String rest = hasPlus ? trimmed.substring(1) : trimmed;
        // Strip spaces, dashes and brackets; anything else left over is not a valid number.
        String cleaned = rest.replaceAll("[\\s()\\-]", "");
        if (cleaned.isEmpty() || !cleaned.matches("[0-9]+")) {
            throw new OwnerTelephoneInvalidException(raw);
        }
        String digits;
        if (hasPlus) {
            digits = cleaned;
        }
        else {
            // Assume the default country code, dropping a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new OwnerTelephoneInvalidException(raw);
        }
        return "+" + digits;
    }
}
