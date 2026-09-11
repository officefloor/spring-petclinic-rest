package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Normalizes telephone numbers to E.164 form.
 *
 * <p>Spaces, dashes and brackets are stripped. When a leading {@code '+'} (an explicit
 * country code) is present it is kept as-is; otherwise country code {@code '+61'} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. The
 * result must be {@code '+'} followed by 8 to 15 digits, else it cannot form a valid
 * E.164 number.
 *
 * <p>For a recognised country code the national number (the digits after the code) must
 * also be exactly the length that country uses: {@code '+61'} requires 9 national digits
 * and {@code '+1'} requires 10. A wrong length yields {@code null}. Country codes not
 * listed fall back to the generic 8-to-15 total-digit rule.
 *
 * <p>E.g. {@code "0412 345 678"} -> {@code "+61412345678"} and
 * {@code "+64 21 123 456"} -> {@code "+6421123456"}.
 */
public final class Telephones {

    /** Recognised country code (without the {@code '+'}) -> required national-number length. */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of(
            "61", 9,
            "1", 10);

    private Telephones() {
    }

    /**
     * Converts a telephone number to its E.164 form, or returns {@code null} when the
     * input cannot form a valid E.164 number.
     */
    public static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        for (Map.Entry<String, Integer> country : NATIONAL_LENGTHS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code)) {
                return digits.length() - code.length() == country.getValue() ? "+" + digits : null;
            }
        }
        return "+" + digits;
    }

    /**
     * Formats a stored telephone number for humans: the country code, a space, then the
     * national digits grouped in threes (e.g. {@code "+61412345678"} ->
     * {@code "+61 412 345 678"}). The input is first normalized via {@link #toE164};
     * when it cannot form a valid E.164 number {@code null} is returned.
     *
     * <p>For a recognised country code the code is split off first so it stays intact;
     * otherwise the digits after the {@code '+'} are grouped in threes as a whole.
     */
    public static String toDisplay(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            return null;
        }
        String digits = e164.substring(1);
        for (Map.Entry<String, Integer> country : NATIONAL_LENGTHS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code) && digits.length() - code.length() == country.getValue()) {
                return "+" + code + " " + groupInThrees(digits.substring(code.length()));
            }
        }
        return "+" + groupInThrees(digits);
    }

    /** Groups digits into space-separated runs of three, from the left. */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }
}
