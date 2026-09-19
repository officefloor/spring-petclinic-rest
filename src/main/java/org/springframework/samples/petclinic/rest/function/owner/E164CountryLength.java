package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Per-country rule for the national-number length an E.164 telephone must carry after its
 * country calling code: '+61' (Australia) requires 9 national digits, '+1' (NANP) requires
 * 10. A number whose country code is not listed here has no per-country constraint beyond
 * {@link TelephoneNormalizer}'s generic 8-15 digit bound. Kept separate from
 * {@link TelephoneNormalizer} so canonicalization (used to compare against stored owners)
 * stays lenient while the create request is held to the stricter, country-specific length.
 */
final class E164CountryLength {

    /** Country calling code -> required national-number length. Matched longest-code-first,
     *  so a code that is a prefix of another still resolves to the more specific rule. */
    private static final Map<String, Integer> NATIONAL_DIGITS = Map.of(
            "61", 9,
            "1", 10);

    private E164CountryLength() {
    }

    /**
     * Returns {@code true} when {@code e164} (a '+' followed by digits, as produced by
     * {@link TelephoneNormalizer#toE164}) carries the national-number length required for its
     * country code, or when its country code has no configured length. A {@code null} or
     * non-E.164 argument returns {@code false}.
     */
    static boolean isNationalLengthValid(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return false;
        }
        String digits = e164.substring(1);
        String code = longestMatchingCode(digits);
        if (code == null) {
            return true;
        }
        return digits.length() - code.length() == NATIONAL_DIGITS.get(code);
    }

    /** The longest configured country code that prefixes {@code digits}, or {@code null}. */
    private static String longestMatchingCode(String digits) {
        String best = null;
        for (String code : NATIONAL_DIGITS.keySet()) {
            if (digits.startsWith(code) && (best == null || code.length() > best.length())) {
                best = code;
            }
        }
        return best;
    }
}
