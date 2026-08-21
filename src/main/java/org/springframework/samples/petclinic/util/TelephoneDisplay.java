package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Formats a stored E.164 telephone for humans: the country code, a space, then the national
 * digits (the digits after the country code) grouped in threes. So {@code "+61412345678"}
 * becomes {@code "+61 412 345 678"}. The raw {@code telephone} stays in E.164 form; this is the
 * separate {@code telephoneDisplay} view derived at response time.
 */
public final class TelephoneDisplay {

    /** Known country codes (leading '+' included), used to split off the national number. */
    private static final Map<String, Integer> COUNTRY_CODE_LENGTHS = Map.of("+61", 3, "+1", 2);

    private TelephoneDisplay() {
    }

    /**
     * Formats {@code e164} as country code, a space, then the national digits grouped in threes.
     *
     * @param e164 the stored E.164 telephone (leading '+' and digits), or {@code null}.
     * @return the human-readable telephone, or {@code null} when {@code e164} is {@code null}.
     */
    public static String of(String e164) {
        if (e164 == null) {
            return null;
        }
        int codeLength = countryCodeLength(e164);
        String countryCode = e164.substring(0, codeLength);
        String national = e164.substring(codeLength);
        StringBuilder display = new StringBuilder(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
    }

    /** Length of the leading country code (including '+'); falls back to a single-digit code. */
    private static int countryCodeLength(String e164) {
        for (Map.Entry<String, Integer> entry : COUNTRY_CODE_LENGTHS.entrySet()) {
            if (e164.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return Math.min(2, e164.length());
    }
}
