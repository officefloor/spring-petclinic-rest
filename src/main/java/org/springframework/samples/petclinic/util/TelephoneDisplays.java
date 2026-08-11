package org.springframework.samples.petclinic.util;

import java.util.List;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Formats an owner's stored E.164 telephone for human display, without altering the raw
 * {@code telephone} field. The display form is a {@code '+'} and the country code, a
 * space, then the national digits grouped in threes from the left. So the stored
 * {@code "+61412345678"} displays as {@code "+61 412 345 678"}.
 *
 * <p>Country codes are recognized from the same set the E.164 normalizer knows, matched
 * longest first so the longest prefix wins ({@code "61"} before {@code "1"}). A number
 * whose country code is not recognized still displays: its whole digit run (after the
 * {@code '+'}) is grouped in threes. Anything not in {@code '+'}-then-digits E.164 form
 * (null, blank, or containing other characters) is returned unchanged.
 */
public final class TelephoneDisplays {

    /** Recognized country codes, longest first so the longest matching prefix wins. */
    private static final List<String> COUNTRY_CODES = List.of("61", "1");

    private TelephoneDisplays() {
    }

    /**
     * @param owner the owner (must not be {@code null}).
     * @return the owner's telephone formatted for display, or the raw value when it is
     *         not in {@code '+'}-then-digits E.164 form.
     */
    public static String displayFor(Owner owner) {
        return displayFor(owner.getTelephone());
    }

    /**
     * @param telephone a telephone number, expected in E.164 form ({@code '+'} then digits).
     * @return the number formatted as country code, space, national digits grouped in
     *         threes; or {@code telephone} unchanged when it is not in E.164 form.
     */
    public static String displayFor(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (digits.isEmpty() || !digits.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return telephone;
        }
        String code = "";
        for (String candidate : COUNTRY_CODES) {
            if (digits.startsWith(candidate) && digits.length() > candidate.length()) {
                code = candidate;
                break;
            }
        }
        String national = digits.substring(code.length());
        return "+" + code + " " + groupInThrees(national);
    }

    /** Groups the digits into space-separated runs of three, from the left. */
    private static String groupInThrees(String national) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(national.charAt(i));
        }
        return sb.toString();
    }
}
