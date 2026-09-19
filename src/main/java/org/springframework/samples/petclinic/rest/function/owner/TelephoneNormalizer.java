package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Shared telephone normalization to E.164 form. Strips spaces, dashes and brackets, then:
 * keeps a leading '+' and country code when present; otherwise assumes country code '+61'
 * and drops a single leading '0' from the national digits. The result must carry 8 to 15
 * digits after the '+'. Used both to canonicalize an incoming request's telephone and to
 * compare it against the telephones already stored on other owners.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * Returns the E.164 representation of {@code raw} (a leading '+' followed by 8-15
     * digits), or {@code null} when it cannot form a valid E.164 number.
     */
    static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
