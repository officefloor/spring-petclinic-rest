package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Shared handling for the owner {@code telephone}. Numbers are stored in E.164 form:
 * a leading {@code '+'} and country code are kept when present; otherwise country code
 * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national
 * digits. Spaces, dashes and brackets are stripped, and the result must have 8 to 15
 * digits after the {@code '+'}.
 *
 * <p>So {@code '0412 345 678'} is stored as {@code '+61412345678'}.
 */
final class Telephones {

    // Formatting characters allowed inside a supplied number: spaces, dashes and brackets.
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()]");

    // E.164: a '+' followed by 8 to 15 digits.
    private static final Pattern E164 = Pattern.compile("\\+\\d{8,15}");

    private Telephones() {
    }

    /**
     * Normalize a telephone to E.164 form.
     *
     * @throws InvalidTelephoneException when the value cannot form a valid E.164 number.
     */
    static String toE164(String telephone) throws InvalidTelephoneException {
        String stripped = SEPARATORS.matcher(telephone == null ? "" : telephone).replaceAll("");
        String e164;
        if (stripped.startsWith("+")) {
            // Keep the leading '+' and the country code exactly as supplied.
            e164 = stripped;
        }
        else {
            // No country code: assume '+61' and drop a single leading '0' from the national digits.
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            e164 = "+61" + national;
        }
        if (!E164.matcher(e164).matches()) {
            throw new InvalidTelephoneException(
                    "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        }
        return e164;
    }
}
