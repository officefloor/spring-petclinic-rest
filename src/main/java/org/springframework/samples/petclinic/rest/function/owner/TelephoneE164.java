package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Converts raw telephone input into E.164 form. Spaces, dashes and brackets are stripped; a leading
 * {@code '+'} and its country code are kept when present, otherwise country code {@code '+61'} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. The result must carry
 * 8 to 15 digits after the {@code '+'}. So {@code '0412 345 678'} becomes {@code '+61412345678'}.
 *
 * <p>Not an OfficeFloor function class — a plain helper shared by the telephone steps.
 */
public final class TelephoneE164 {

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
        return "+" + digits;
    }
}
