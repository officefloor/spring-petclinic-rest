package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes a telephone number to E.164 form.
 *
 * <p>Spaces, dashes and brackets are stripped. A leading {@code '+'} and its country code are
 * kept as-is; otherwise country code {@code +61} is assumed and a single leading {@code '0'} is
 * dropped from the national digits. The result must have 8 to 15 digits after the {@code '+'} or
 * the number is rejected as an {@link InvalidTelephoneException}. So {@code "0412 345 678"}
 * becomes {@code "+61412345678"} and {@code "+64 21 123 456"} becomes {@code "+6421123456"}.
 */
final class OwnerTelephone {

    private OwnerTelephone() {
    }

    static String toE164(String input) throws InvalidTelephoneException {
        if (input == null) {
            throw new InvalidTelephoneException(null);
        }
        String cleaned = input.replaceAll("[\\s()\\[\\]-]", "");
        boolean hasPlus = cleaned.startsWith("+");
        String rest = hasPlus ? cleaned.substring(1) : cleaned;
        if (rest.isEmpty() || !rest.chars().allMatch(c -> c >= '0' && c <= '9')) {
            throw new InvalidTelephoneException(input);
        }
        String digits;
        if (hasPlus) {
            digits = rest;
        }
        else {
            if (rest.startsWith("0")) {
                rest = rest.substring(1);
            }
            digits = "61" + rest;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new InvalidTelephoneException(input);
        }
        return "+" + digits;
    }
}
