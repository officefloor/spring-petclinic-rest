package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;
import java.util.Map;

/**
 * Shared telephone canonicalization for the create-owner pipeline: reduces a telephone to
 * its E.164 form so validation, uniqueness checks and storage all compare the same value.
 */
final class OwnerTelephones {

    /**
     * Country calling code (E.164 digits after the {@code '+'}) mapped to the exact number
     * of national digits a valid number for that country carries. Codes not listed here
     * carry no per-country length rule.
     */
    private static final Map<String, Integer> NATIONAL_DIGITS = Map.of("1", 10, "61", 9);

    private OwnerTelephones() {
    }

    /**
     * Canonicalizes {@code telephone} to E.164 (a leading {@code '+'} followed by 8 to 15
     * digits), or returns {@code null} when it cannot form a valid E.164 number.
     *
     * <p>Spaces, dashes and brackets are stripped. A leading {@code '+'} keeps the given
     * country code; otherwise country code {@code +61} is assumed and a single leading
     * {@code '0'} is dropped from the national digits. So {@code "0412 345 678"} becomes
     * {@code "+61412345678"} and {@code "+64 21 123 456"} becomes {@code "+6421123456"}.
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Whether the E.164 {@code telephone}'s national-number length is correct for its
     * country calling code (e.g. {@code +61} requires 9 national digits, {@code +1}
     * requires 10). A country code with no known length rule is accepted.
     *
     * @param telephone an E.164 number (as produced by {@link #toE164(String)}).
     */
    static boolean hasValidNationalLength(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return false;
        }
        String digits = telephone.substring(1);
        // Longest code first so "+61" is not matched as "+6"/"+1".
        String code = NATIONAL_DIGITS.keySet().stream()
                .filter(digits::startsWith)
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
        if (code == null) {
            return true; // no per-country length rule
        }
        return digits.length() - code.length() == NATIONAL_DIGITS.get(code);
    }
}
