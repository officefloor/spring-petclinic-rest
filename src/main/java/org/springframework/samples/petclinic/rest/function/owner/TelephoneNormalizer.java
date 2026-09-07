package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Single home for owner telephone normalization. Both the create pipeline's
 * {@link BuildOwner} (which stores the canonical form) and
 * {@link OwnerIdentity} (whose identity key detects duplicates) go through here, so the
 * stored form and the duplicate-comparison stay defined in one place.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /** Default country code assumed when the supplied number carries no explicit '+'. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * Required national-number length per country code. Insertion order is longest code
     * first so prefix matching resolves the most specific country. A code not listed here
     * is not length-checked beyond the generic 8-to-15 digit rule.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTHS.put("61", 9); // Australia: +61 needs 9 national digits
        NATIONAL_LENGTHS.put("1", 10); // NANP: +1 needs 10 national digits
    }

    /**
     * Canonical E.164 form to store for a supplied telephone. Spaces, dashes and brackets
     * are stripped. A leading '+' with its country code is kept; otherwise country code
     * '+61' is assumed and a single leading '0' is dropped from the national digits. The
     * result must have 8 to 15 digits after the '+', and — for a recognised country code —
     * the national number must match that country's required length ('+61' => 9 national
     * digits, '+1' => 10); otherwise the request is rejected with a 400. So
     * '0412 345 678' becomes '+61412345678'.
     */
    static String normalize(String telephone) throws InvalidTelephoneException {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidTelephoneException(telephone);
        }
        return e164;
    }

    /**
     * Comparison key used to detect duplicate telephones — the E.164 form. Lenient (never
     * throws) so whatever is already stored on existing owners still compares; when a value
     * cannot form valid E.164 it falls back to its stripped characters.
     */
    static String comparisonKey(String telephone) {
        String e164 = toE164(telephone);
        return e164 != null ? e164 : stripSeparators(telephone);
    }

    /**
     * Produce the E.164 string for a telephone, or {@code null} when it cannot form a valid
     * one (non-digit content remaining, not 8 to 15 digits after the '+', or a national
     * number of the wrong length for its recognised country code).
     */
    private static String toE164(String telephone) {
        String cleaned = stripSeparators(telephone);
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        if (!hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * For a recognised country code, the national number (digits after the code) must have
     * exactly the length that country requires. Country codes not listed in
     * {@link #NATIONAL_LENGTHS} are accepted on the generic 8-to-15 digit rule alone.
     */
    private static boolean hasValidNationalLength(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTHS.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                return nationalLength == entry.getValue();
            }
        }
        return true;
    }

    private static String stripSeparators(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("[\\s()\\-]", "");
    }
}
