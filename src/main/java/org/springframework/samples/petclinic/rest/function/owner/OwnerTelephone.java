package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes an owner {@code telephone} to E.164 form. Spaces, dashes and brackets are stripped; a
 * value that already carries a leading {@code '+'} and country code keeps them, otherwise country
 * code {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national digits.
 * The result must be a {@code '+'} followed by 8 to 15 digits. So {@code '0412 345 678'} becomes
 * {@code '+61412345678'} and {@code '+64 21 123 456'} becomes {@code '+6421123456'}.
 *
 * <p>For known country codes the national-number length is also checked: {@code '+61'} requires 9
 * national digits and {@code '+1'} requires 10. A number whose national part is the wrong length for
 * its country is rejected. Country codes not listed here are only length-checked by the E.164 range.
 *
 * <p>{@link #toDisplay} renders a stored E.164 number for humans: the {@code '+'} country code, a
 * space, then the national digits grouped in threes (e.g. {@code '+61412345678'} becomes
 * {@code '+61 412 345 678'}).
 */
public final class OwnerTelephone {

    /** E.164: a {@code '+'} followed by 8 to 15 digits. */
    private static final Pattern E164 = Pattern.compile("\\+[0-9]{8,15}");

    /** Formatting characters removed before interpreting the number: spaces, dashes and brackets. */
    private static final Pattern FORMATTING = Pattern.compile("[\\s()\\-]");

    /**
     * Country code (digits after the {@code '+'}) to required national-number length. Ordered
     * longest-prefix first so {@code '61'} is matched before the shorter {@code '1'}.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = new LinkedHashMap<>();
    static {
        NATIONAL_LENGTHS.put("61", 9);
        NATIONAL_LENGTHS.put("1", 10);
    }

    private OwnerTelephone() {
    }

    /**
     * @return the telephone in E.164 form.
     * @throws InvalidTelephoneException when the value cannot form a valid E.164 number.
     */
    static String toE164(String telephone) throws InvalidTelephoneException {
        if (telephone == null) {
            throw new InvalidTelephoneException(null);
        }
        String stripped = FORMATTING.matcher(telephone).replaceAll("");
        String e164;
        if (stripped.startsWith("+")) {
            e164 = stripped;
        } else {
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            e164 = "+61" + national;
        }
        if (!E164.matcher(e164).matches()) {
            throw new InvalidTelephoneException(telephone);
        }
        String digits = e164.substring(1);
        for (Map.Entry<String, Integer> country : NATIONAL_LENGTHS.entrySet()) {
            if (digits.startsWith(country.getKey())) {
                int nationalLength = digits.length() - country.getKey().length();
                if (nationalLength != country.getValue()) {
                    throw new InvalidTelephoneException(telephone);
                }
                break;
            }
        }
        return e164;
    }

    /**
     * Formats a stored E.164 telephone for display: the {@code '+'} country code, a space, then the
     * national digits grouped in threes. Known country codes ({@code '+61'}, {@code '+1'}) split the
     * country code from the national number; otherwise the whole run of digits is grouped in threes.
     *
     * @param e164 the stored E.164 number (a {@code '+'} followed by digits).
     * @return the human-readable form, e.g. {@code '+61 412 345 678'}, or {@code null} when
     *         {@code e164} is null.
     */
    public static String toDisplay(String e164) {
        if (e164 == null) {
            return null;
        }
        String digits = e164.startsWith("+") ? e164.substring(1) : e164;
        String countryCode = null;
        String national = digits;
        for (String code : NATIONAL_LENGTHS.keySet()) {
            if (digits.startsWith(code)) {
                countryCode = code;
                national = digits.substring(code.length());
                break;
            }
        }
        String grouped = groupInThrees(national);
        return countryCode == null ? "+" + grouped : "+" + countryCode + " " + grouped;
    }

    /** Groups a run of digits into space-separated groups of three, from the left. */
    private static String groupInThrees(String digits) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                builder.append(' ');
            }
            builder.append(digits.charAt(i));
        }
        return builder.toString();
    }
}
