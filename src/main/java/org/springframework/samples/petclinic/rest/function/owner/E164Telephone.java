package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Converts a supplied telephone into E.164 form. Spaces, dashes and brackets are stripped; a leading
 * '+' with its country code is kept when present, otherwise country code '+61' is assumed and a single
 * leading '0' is dropped from the national digits. The result must carry 8 to 15 digits after the '+',
 * otherwise it cannot form a valid E.164 number and an {@link InvalidTelephoneException} (400) is thrown.
 */
final class E164Telephone {

    private E164Telephone() {
    }

    static String normalize(String telephone) throws InvalidTelephoneException {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s()\\[\\]-]", "");
        boolean hasPlus = cleaned.startsWith("+");
        String rest = hasPlus ? cleaned.substring(1) : cleaned;
        if (rest.isEmpty() || !rest.chars().allMatch(Character::isDigit)) {
            throw new InvalidTelephoneException(telephone);
        }
        String digits;
        if (hasPlus) {
            digits = rest;
        }
        else {
            String national = rest.startsWith("0") ? rest.substring(1) : rest;
            digits = "61" + national;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new InvalidTelephoneException(telephone);
        }
        return "+" + digits;
    }
}
