package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * Formats a stored E.164 telephone for humans: the country code, a space, then the national digits
 * grouped in threes (e.g. {@code "+61412345678"} -> {@code "+61 412 345 678"}). The country code is
 * the longest leading 1-3 digit prefix that is a known calling code, defaulting to two digits (the
 * Australian {@code +61} the app assumes). Anything not in E.164 form is returned unchanged.
 */
public final class TelephoneDisplay {

    private static final Set<String> CALLING_CODES = Set.of("1", "7", "44", "61", "64", "33", "49", "81", "86", "91");

    private TelephoneDisplay() {
    }

    public static String of(String e164) {
        if (e164 == null || !e164.startsWith("+") || !e164.substring(1).matches("\\d+")) {
            return e164;
        }
        String digits = e164.substring(1);
        int ccLen = countryCodeLength(digits);
        return "+" + digits.substring(0, ccLen) + " " + group(digits.substring(ccLen));
    }

    private static int countryCodeLength(String digits) {
        for (int len = Math.min(3, digits.length()); len >= 1; len--) {
            if (CALLING_CODES.contains(digits.substring(0, len))) {
                return len;
            }
        }
        return Math.min(2, digits.length());
    }

    private static String group(String national) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return grouped.toString();
    }
}
