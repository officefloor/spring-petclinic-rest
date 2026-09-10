package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Normalizes an owner telephone number into E.164 form.
 *
 * <p>Rules: strip spaces, dashes and brackets. If a leading {@code '+'} (and therefore an
 * explicit country code) is present it is kept as-is. Otherwise the default country code
 * {@code +61} is assumed and a single leading {@code '0'} is dropped from the national digits.
 * The result must carry 8 to 15 digits after the {@code '+'}. Anything that cannot form a
 * valid E.164 number raises {@link InvalidOwnerTelephoneException} so the endpoint responds 400.
 *
 * <p>For recognised country codes the national number (the digits after the country code) must
 * also have the exact length that country requires: {@code +61} (Australia) needs 9 national
 * digits and {@code +1} (NANP) needs 10. A wrong national-number length raises
 * {@link InvalidOwnerTelephoneException} so the endpoint responds 400.
 *
 * <p>So {@code "0412 345 678"} becomes {@code "+61412345678"} and {@code "+64 21 123 456"}
 * becomes {@code "+6421123456"}.
 */
public final class OwnerTelephone {

    /** Default country code assumed when the input carries no explicit {@code '+'} prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    /**
     * National-number length required per country code. Ordered longest code first so the most
     * specific prefix wins when matching.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH.put("61", 9); // Australia
        NATIONAL_LENGTH.put("1", 10); // North American Numbering Plan
    }

    private OwnerTelephone() {
    }

    /**
     * Convert a raw telephone string into E.164 form, or throw when it cannot form a valid one.
     */
    static String toE164(String raw) throws InvalidOwnerTelephoneException {
        if (raw == null) {
            throw new InvalidOwnerTelephoneException("null");
        }
        // Strip spaces, dashes and brackets.
        String cleaned = raw.replaceAll("[\\s()\\[\\]-]", "");
        boolean explicitCountryCode = cleaned.startsWith("+");
        String digits = explicitCountryCode ? cleaned.substring(1) : cleaned;
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            throw new InvalidOwnerTelephoneException(raw);
        }
        String e164Digits;
        if (explicitCountryCode) {
            e164Digits = digits;
        }
        else {
            // No explicit country code: assume the default and drop one leading national '0'.
            String national = digits.startsWith("0") ? digits.substring(1) : digits;
            e164Digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (e164Digits.length() < MIN_DIGITS || e164Digits.length() > MAX_DIGITS) {
            throw new InvalidOwnerTelephoneException(raw);
        }
        // For a recognised country code, the national number must have the exact required length.
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTH.entrySet()) {
            String countryCode = entry.getKey();
            if (e164Digits.startsWith(countryCode)) {
                int national = e164Digits.length() - countryCode.length();
                if (national != entry.getValue()) {
                    throw new InvalidOwnerTelephoneException(raw);
                }
                break;
            }
        }
        return "+" + e164Digits;
    }

    /**
     * Format a stored telephone for humans: the country code, a space, then the national digits
     * grouped in threes (e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}). Values
     * that cannot form a valid E.164 number are returned unchanged.
     */
    public static String toDisplay(String raw) {
        String canonical = canonical(raw);
        if (!canonical.startsWith("+")) {
            // Not a valid E.164 value (e.g. legacy data) - nothing sensible to group.
            return raw;
        }
        String digits = canonical.substring(1);
        for (String countryCode : NATIONAL_LENGTH.keySet()) {
            if (digits.startsWith(countryCode)) {
                String national = digits.substring(countryCode.length());
                return "+" + countryCode + " " + groupInThrees(national);
            }
        }
        // Unrecognised country code: group everything after the '+' in threes.
        return "+" + groupInThrees(digits);
    }

    /** Group a run of digits into space-separated groups of three, from the left. */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder(digits.length() + digits.length() / 3);
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }

    /**
     * Canonical E.164 form for comparison, falling back to a digits-only form for values that
     * cannot form a valid E.164 number (e.g. legacy data).
     */
    static String canonical(String raw) {
        try {
            return toE164(raw);
        }
        catch (InvalidOwnerTelephoneException ex) {
            return raw == null ? "" : raw.replaceAll("\\D", "");
        }
    }
}
