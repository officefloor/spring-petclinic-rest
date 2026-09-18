package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Telephone normalization shared by the owner pipelines: converts a supplied telephone to
 * canonical E.164 form (a leading {@code +}, country code, then national digits), so an
 * owner's telephone is stored, returned and compared in one representation.
 */
public final class OwnerTelephone {

    /** Country code assumed when the input carries no explicit {@code +} prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * National-number digit count required per country code. The national number (the digits
     * after the country code) must be exactly this long for the number to be valid, e.g.
     * {@code +61} takes 9 national digits and {@code +1} takes 10.
     */
    private static final Map<String, Integer> NATIONAL_DIGITS = Map.of("1", 10, "61", 9);

    /** Known country codes, longest first, so the longest matching prefix wins. */
    private static final List<String> COUNTRY_CODES = NATIONAL_DIGITS.keySet().stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .toList();

    /** E.164 allows between 8 and 15 digits after the {@code +} for other country codes. */
    private static final int MIN_DIGITS = 8;
    private static final int MAX_DIGITS = 15;

    private OwnerTelephone() {
    }

    /**
     * The E.164 form of {@code telephone}, or {@link Optional#empty()} when it cannot form a
     * valid E.164 number. Spaces, dashes and brackets are stripped. A leading {@code +} keeps
     * the given country code; otherwise country code {@code +61} is assumed and a single
     * leading {@code 0} is dropped from the national digits. For a recognised country code the
     * national number must have exactly the length that code requires ({@code +61} → 9,
     * {@code +1} → 10); otherwise the full number must carry 8 to 15 digits after the {@code +}.
     */
    public static Optional<String> toE164(String telephone) {
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
        if (!digits.matches("[0-9]+") || !hasValidLength(digits)) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }

    /**
     * Whether the country-code-plus-national {@code digits} has an acceptable length: a
     * recognised country code fixes the national-number length, otherwise the whole number
     * must sit within the generic E.164 range.
     */
    private static boolean hasValidLength(String digits) {
        for (String code : COUNTRY_CODES) {
            if (digits.startsWith(code)) {
                int nationalLength = digits.length() - code.length();
                return nationalLength == NATIONAL_DIGITS.get(code);
            }
        }
        return digits.length() >= MIN_DIGITS && digits.length() <= MAX_DIGITS;
    }
}
