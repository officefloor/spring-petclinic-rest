package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.util.Telephone;

/**
 * Shared telephone normalization for the owner pipelines: convert a raw telephone to
 * E.164 form. Spaces, dashes and brackets are stripped; a number that already carries
 * a leading {@code '+'} keeps its country code, otherwise Australian country code
 * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the
 * national digits. The result is a {@code '+'} followed by 8 to 15 digits.
 *
 * <p>The national-number length must also match the country code: {@code '+61'}
 * (Australia) requires 9 national digits and {@code '+1'} (NANP) requires 10. A number
 * whose national part is the wrong length for its country is not valid E.164 here.
 *
 * <p>Used by {@link NormalizeOwnerTelephone}, which rejects a number that cannot form
 * valid E.164 and stores the normalized value, so every owner's telephone is canonical
 * before it becomes part of the owner's identity key.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * @return the E.164 representation of {@code telephone}, or {@code null} when it
     *         cannot form a valid E.164 number.
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = "61" + cleaned;
        }
        if (!digits.matches("[0-9]{8,15}") || !hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * @return whether the national digits following a recognised country code have the
     *         length that code requires. Unrecognised country codes are left to the
     *         general 8-to-15-digit E.164 shape check.
     */
    private static boolean hasValidNationalLength(String digits) {
        String code = Telephone.countryCode(digits);
        if (code == null) {
            return true;
        }
        return digits.length() - code.length() == Telephone.nationalLength(code);
    }
}
