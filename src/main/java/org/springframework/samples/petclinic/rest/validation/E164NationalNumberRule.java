package org.springframework.samples.petclinic.rest.validation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Per-country rule for the national-number length of an E.164 telephone.
 *
 * <p>An E.164 number is a {@code '+'} followed by a country calling code and a national number.
 * Each country fixes how many national digits it uses; this holds those known lengths (e.g. the
 * {@code +61} Australian format carries 9 national digits, the {@code +1} North American format
 * carries 10) and checks a normalized E.164 number against them. Numbers whose country code has no
 * known length are left unconstrained (beyond the general E.164 shape enforced by
 * {@link TelephoneNormalizer#toE164(String)}).
 */
public final class E164NationalNumberRule {

    /** Known country calling codes mapped to their required national-number digit count. */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of(
            "1", 10,   // North American Numbering Plan
            "61", 9);  // Australia

    /** Country codes checked longest-first, so a longer code wins over a shorter prefix of it. */
    private static final List<String> CODES_BY_LENGTH = NATIONAL_LENGTHS.keySet().stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .collect(Collectors.toList());

    private E164NationalNumberRule() {
    }

    /**
     * Whether the national number of a normalized E.164 string has the length its country code
     * requires. A number whose country code has no known length rule is accepted.
     *
     * @param e164 a normalized E.164 string ({@code '+'} followed by digits), as produced by
     *             {@link TelephoneNormalizer#toE164(String)}; may be {@code null}
     * @return {@code true} when the national-number length matches the country code (or the country
     *         code is unknown), {@code false} otherwise (including for a {@code null} input)
     */
    public static boolean hasValidNationalLength(String e164) {
        if (e164 == null) {
            return false;
        }
        String digits = e164.startsWith("+") ? e164.substring(1) : e164;
        for (String code : CODES_BY_LENGTH) {
            if (digits.startsWith(code)) {
                return digits.length() - code.length() == NATIONAL_LENGTHS.get(code);
            }
        }
        return true;
    }
}
