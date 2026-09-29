package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * The single definition of the E.164 form of a telephone number. Shared by
 * {@link NormalizeOwnerTelephone} (which stores the E.164 value) and {@link IdentityKey}
 * (which folds it into the duplicate-detection key), so both apply the same rule and cannot drift apart.
 *
 * <p>The rule: strip spaces, dashes and brackets. When a leading {@code '+'} is present, keep it and
 * the country code that follows; otherwise assume country code {@code '+61'} and drop a single leading
 * {@code '0'} from the national digits. The result must carry 8 to 15 digits after the {@code '+'};
 * anything else cannot form valid E.164 and yields {@code null}.
 *
 * <p>The national-number length is also checked against the country code: {@code '+61'} (Australia)
 * requires exactly 9 national digits and {@code '+1'} (NANP) requires exactly 10. A number for one of
 * those countries whose national part is the wrong length cannot form valid E.164 and yields
 * {@code null} (rejected with a 400). Other country codes keep only the generic 8-to-15 length rule.
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method.
 */
final class TelephoneNormalizer {

    /**
     * Country code (the digits after the {@code '+'}) to its required national-number length. Country
     * codes absent here keep only the generic 8-to-15 rule. Adding a country is a new entry, not a new
     * branch.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH = Map.of("61", 9, "1", 10);

    /**
     * Returns the E.164 form of the given telephone, or {@code null} when it cannot form a valid one.
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        // Strip spaces, dashes and brackets.
        String cleaned = telephone.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Explicit country code: keep the digits that follow the '+'.
            digits = cleaned.substring(1);
        }
        else {
            // No country code: assume +61 and drop a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        // Everything after the '+' must be 8 to 15 digits.
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        // Per-country national-number length: +61 (Australia) needs 9 national digits, +1 (NANP)
        // needs 10. Country codes not listed keep only the generic 8-to-15 rule above.
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTH.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)
                    && digits.length() - countryCode.length() != entry.getValue()) {
                return null;
            }
        }
        return "+" + digits;
    }

    private TelephoneNormalizer() {
    }
}
