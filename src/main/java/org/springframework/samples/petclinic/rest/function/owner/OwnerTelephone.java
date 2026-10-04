package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the owner telephone normalisation rule used across the
 * {@code POST /owners} pipeline: a telephone is canonicalised to E.164 form. Both
 * {@link NormalizeOwnerTelephone} (which stores the canonical value and rejects an input that
 * cannot form one) and {@link CheckOwnerTelephoneUnique} (which compares stored values) must
 * agree on this definition, so it lives here rather than being re-implemented at each site.
 *
 * <p>The rule: strip spaces, dashes and brackets; keep a leading {@code '+'} and country code
 * when present, otherwise assume country code {@code +61} and drop a single leading {@code '0'}
 * from the national digits; the result must be a {@code '+'} followed by 8 to 15 digits. So
 * {@code "0412 345 678"} canonicalises to {@code "+61412345678"} and {@code "+64 21 123 456"}
 * to {@code "+6421123456"}.
 */
final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /**
     * Canonicalise {@code telephone} to E.164 form, or return {@code null} when it cannot form a
     * valid E.164 number (null/blank input, a non-digit left after stripping separators, or
     * fewer than 8 or more than 15 digits after the {@code '+'}).
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        // Strip spaces, dashes and brackets, leaving an optional leading '+' and the digits.
        String cleaned = telephone.replaceAll("[\\s()\\[\\]-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Leading '+' and country code present: keep as-is.
            digits = cleaned.substring(1);
        } else {
            // No country code: assume +61 and drop a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
