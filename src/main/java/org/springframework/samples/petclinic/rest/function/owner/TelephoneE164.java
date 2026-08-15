package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Shared normalization for an owner's {@code telephone} to E.164 form. Spaces, dashes and
 * brackets are stripped. A leading '+' and country code are kept when present; otherwise the
 * country code '+61' is assumed and a single leading '0' is dropped from the national digits.
 * The result must have 8 to 15 digits after the '+'. So {@code "0412 345 678"} becomes {@code
 * "+61412345678"}. In addition, the national-number length must match the country code: '+61'
 * requires 9 national digits and '+1' requires 10. A number that cannot form valid E.164, or
 * whose national length is wrong for its country, is rejected with 400 via {@link
 * InvalidTelephoneException}.
 */
final class TelephoneE164 {

    /** Characters removed before interpreting the number. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()]");

    /** 8 to 15 digits, the E.164 length bound (country code plus national digits). */
    private static final Pattern E164_DIGITS = Pattern.compile("\\d{8,15}");

    /**
     * Country code (without '+') to the exact number of national digits it requires. Ordered
     * longest-prefix-first so a code is matched before any shorter code that is a suffix of it.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH.put("61", 9);
        NATIONAL_LENGTH.put("1", 10);
    }

    private TelephoneE164() {
    }

    /**
     * Returns the E.164 string (a '+' followed by 8 to 15 digits) for the given raw telephone.
     *
     * @throws InvalidTelephoneException when no valid E.164 string can be formed.
     */
    static String normalize(String telephone) throws InvalidTelephoneException {
        if (telephone == null) {
            throw new InvalidTelephoneException(null);
        }
        String cleaned = SEPARATORS.matcher(telephone).replaceAll("");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!E164_DIGITS.matcher(digits).matches()) {
            throw new InvalidTelephoneException(telephone);
        }
        for (Map.Entry<String, Integer> country : NATIONAL_LENGTH.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code)) {
                if (digits.length() - code.length() != country.getValue()) {
                    throw new InvalidTelephoneException(telephone);
                }
                break;
            }
        }
        return "+" + digits;
    }
}
