package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes an owner {@code telephone} to E.164 form. Spaces, dashes and brackets are stripped; a
 * value that already carries a leading {@code '+'} and country code keeps them, otherwise country
 * code {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national digits.
 * The result must be a {@code '+'} followed by 8 to 15 digits. So {@code '0412 345 678'} becomes
 * {@code '+61412345678'} and {@code '+64 21 123 456'} becomes {@code '+6421123456'}.
 */
final class OwnerTelephone {

    /** E.164: a {@code '+'} followed by 8 to 15 digits. */
    private static final Pattern E164 = Pattern.compile("\\+[0-9]{8,15}");

    /** Formatting characters removed before interpreting the number: spaces, dashes and brackets. */
    private static final Pattern FORMATTING = Pattern.compile("[\\s()\\-]");

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
        return e164;
    }
}
