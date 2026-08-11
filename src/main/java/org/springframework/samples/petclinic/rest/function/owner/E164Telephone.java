package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Normalises telephone numbers to <a href="https://en.wikipedia.org/wiki/E.164">E.164</a> form.
 *
 * <p>Spaces, dashes and brackets are stripped. A leading {@code '+'} and its country code are kept
 * as given; otherwise country code {@code +61} is assumed and a single leading {@code '0'} is
 * dropped from the national digits. The result is a {@code '+'} followed by 8 to 15 digits, so
 * {@code "0412 345 678"} becomes {@code "+61412345678"}.
 *
 * <p>For known country codes the national-number length is checked as well: {@code +61} requires 9
 * national digits and {@code +1} requires 10. A number whose national part is the wrong length for
 * its country is rejected (returns {@code null}).
 */
final class E164Telephone {

    /**
     * Country code (without the {@code '+'}) to its required national-number length. Ordered
     * longest-prefix first so a {@code +61} number is matched by {@code "61"} before {@code "1"}.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH.put("61", 9);
        NATIONAL_LENGTH.put("1", 10);
    }

    private E164Telephone() {
    }

    /**
     * Returns the E.164 form of {@code raw}, or {@code null} when it cannot form a valid E.164
     * number: non-digit content after stripping separators, not 8 to 15 digits after the
     * {@code '+'}, or the wrong national-number length for a known country code ({@code +61} needs 9
     * national digits, {@code +1} needs 10).
     */
    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        // Strip spaces, dashes and brackets.
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Keep the leading '+' and country code as given.
            digits = cleaned.substring(1);
        }
        else {
            // No country code: assume '+61' and drop a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        // Enforce the national-number length for recognised country codes.
        for (Map.Entry<String, Integer> country : NATIONAL_LENGTH.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code)) {
                if (digits.length() - code.length() != country.getValue()) {
                    return null;
                }
                break;
            }
        }
        return "+" + digits;
    }
}
