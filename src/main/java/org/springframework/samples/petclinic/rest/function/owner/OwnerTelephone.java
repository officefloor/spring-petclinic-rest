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
 */
final class OwnerTelephone {

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
}
