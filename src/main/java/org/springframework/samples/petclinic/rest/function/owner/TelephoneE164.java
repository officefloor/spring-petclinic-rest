package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Converts raw telephone input into E.164 form. Spaces, dashes and brackets are stripped; a leading
 * {@code '+'} and its country code are kept when present, otherwise country code {@code '+61'} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. The result must carry
 * 8 to 15 digits after the {@code '+'}. So {@code '0412 345 678'} becomes {@code '+61412345678'}.
 *
 * <p>The national-number length is additionally checked against the country code: {@code '+61'}
 * requires 9 national digits and {@code '+1'} requires 10. A number whose national length is wrong
 * for its country is rejected, even when the overall digit count is within 8 to 15.
 *
 * <p>Not an OfficeFloor function class — a plain helper shared by the telephone steps.
 */
public final class TelephoneE164 {

    /** Country code (digits after '+') to its required national-number digit count. */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of(
            "61", 9,
            "1", 10);

    private TelephoneE164() {
    }

    /** Converts to E.164, or throws {@link InvalidTelephoneException} when the input cannot form it. */
    public static String toE164(String telephone) throws InvalidTelephoneException {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            digits = "61" + (cleaned.startsWith("0") ? cleaned.substring(1) : cleaned);
        }
        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(telephone);
        }
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTHS.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                if (nationalLength != entry.getValue()) {
                    throw new InvalidTelephoneException(telephone);
                }
                break;
            }
        }
        return "+" + digits;
    }
}
