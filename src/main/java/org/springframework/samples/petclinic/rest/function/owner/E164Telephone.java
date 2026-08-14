package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Converts a supplied telephone into E.164 form. Spaces, dashes and brackets are stripped; a leading
 * '+' with its country code is kept when present, otherwise country code '+61' is assumed and a single
 * leading '0' is dropped from the national digits. The result must carry 8 to 15 digits after the '+',
 * otherwise it cannot form a valid E.164 number and an {@link InvalidTelephoneException} (400) is thrown.
 *
 * <p>For a recognized country code the national number (the digits after the country code) must also be
 * exactly the length that country requires — '+61' (Australia) requires 9 national digits and '+1'
 * (NANP) requires 10. A wrong national-number length for the country is likewise rejected with a 400.
 */
public final class E164Telephone {

    /** Country code (digits after the '+', without it) -> required national-number length. Ordered
     *  longest-first so the most specific prefix wins when resolving the country code. */
    private static final Map<String, Integer> NATIONAL_LENGTH = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH.put("61", 9); // Australia
        NATIONAL_LENGTH.put("1", 10); // NANP
    }

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

    /**
     * Formats a stored E.164 telephone for humans: the '+' and country code, a space, then the
     * national digits grouped in threes (e.g. {@code +61412345678} becomes {@code +61 412 345 678}).
     * A recognized country code is split off; otherwise the leading digits up to the first three are
     * treated as the country code. Anything that is not a '+' followed by digits is returned verbatim.
     */
    public static String display(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            return e164;
        }
        String code = null;
        for (String candidate : NATIONAL_LENGTH.keySet()) {
            if (digits.startsWith(candidate) && digits.length() > candidate.length()) {
                code = candidate;
                break;
            }
        }
        if (code == null) {
            code = digits.substring(0, Math.min(3, digits.length()));
        }
        String national = digits.substring(code.length());
        StringBuilder sb = new StringBuilder("+").append(code);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }
}
