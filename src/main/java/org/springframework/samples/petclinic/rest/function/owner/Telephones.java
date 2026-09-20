package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.Optional;

/**
 * Shared telephone handling: converts a raw number to its canonical E.164 form so numbers
 * written with different separators (or an explicit country code) compare equal. Used both
 * to normalize a create request before it is persisted and to compare it against existing
 * owners' stored numbers.
 */
final class Telephones {

    /** Country code assumed when the input carries no explicit '+' prefix (Australia). */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * Required national-number length (the digits after the country code) for each country
     * code we recognise: '+61' (Australia) takes 9, '+1' (NANP) takes 10. A number whose
     * country code is not listed is accepted on the generic 8-15 digit range alone.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of("61", 9, "1", 10);

    private Telephones() {
    }

    /**
     * Converts {@code telephone} to E.164 (a leading '+' followed by 8 to 15 digits).
     *
     * <p>Spaces, dashes and brackets are stripped. When the number keeps a leading '+' its
     * country code is preserved; otherwise {@code +61} is assumed and a single leading '0'
     * is dropped from the national digits. Returns empty when the result cannot form valid
     * E.164 (non-digits remain, the digit count is out of range, or the national number is
     * the wrong length for a recognised country code).
     */
    static Optional<String> toE164(String telephone) {
        if (telephone == null) {
            return Optional.empty();
        }
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!digits.matches("[0-9]{8,15}") || !hasValidNationalNumberLength(digits)) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }

    /**
     * Checks the national-number length of {@code digits} (the E.164 digits without the '+')
     * against its country code, using the longest recognised code the number starts with.
     * Unrecognised country codes pass, having already cleared the generic 8-15 digit range.
     */
    private static boolean hasValidNationalNumberLength(String digits) {
        String countryCode = null;
        for (String candidate : NATIONAL_NUMBER_LENGTHS.keySet()) {
            if (digits.startsWith(candidate)
                    && (countryCode == null || candidate.length() > countryCode.length())) {
                countryCode = candidate;
            }
        }
        if (countryCode == null) {
            return true;
        }
        return digits.length() - countryCode.length() == NATIONAL_NUMBER_LENGTHS.get(countryCode);
    }
}
