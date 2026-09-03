package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Per-country E.164 national-number length rule. A valid number is a supported
 * country code followed by exactly that country's national-digit count
 * ('+61' -> 9 national digits, '+1' -> 10). Anything else is rejected.
 */
final class E164Length {

    /** Country calling code (no '+') -> required national-number length. */
    private static final Map<String, Integer> NATIONAL_DIGITS = Map.of("61", 9, "1", 10);

    private E164Length() {
    }

    /** True when {@code digits} (country code + national number, no '+') matches a known country's length. */
    static boolean isValid(String digits) {
        for (Map.Entry<String, Integer> country : NATIONAL_DIGITS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code) && digits.length() - code.length() == country.getValue()) {
                return true;
            }
        }
        return false;
    }
}
