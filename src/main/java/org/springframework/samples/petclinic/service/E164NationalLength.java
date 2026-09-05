package org.springframework.samples.petclinic.service;

import java.util.Map;

/**
 * Validates an E.164 number's national-number length against its country code:
 * '+61' requires 9 national digits, '+1' requires 10. Country codes without a
 * known requirement are accepted unchanged.
 */
public final class E164NationalLength {

    private E164NationalLength() {
    }

    /** Country code (digits, no '+') -> required national-number digit count. */
    private static final Map<String, Integer> REQUIRED = Map.of("1", 10, "61", 9);

    /**
     * @param digits the E.164 digits: country code followed by the national number, no '+'
     * @throws IllegalArgumentException if the national-number length is wrong for the country
     */
    public static void check(String digits) {
        for (Map.Entry<String, Integer> rule : REQUIRED.entrySet()) {
            String code = rule.getKey();
            if (digits.startsWith(code)) {
                if (digits.length() - code.length() != rule.getValue()) {
                    throw new IllegalArgumentException(
                        "National number for +" + code + " must be " + rule.getValue() + " digits: +" + digits);
                }
                return;
            }
        }
    }
}
