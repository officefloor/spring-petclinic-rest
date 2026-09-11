package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalizes telephone numbers to E.164 form.
 *
 * <p>Spaces, dashes and brackets are stripped. When a leading {@code '+'} (an explicit
 * country code) is present it is kept as-is; otherwise country code {@code '+61'} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. The
 * result must be {@code '+'} followed by 8 to 15 digits, else it cannot form a valid
 * E.164 number.
 *
 * <p>E.g. {@code "0412 345 678"} -> {@code "+61412345678"} and
 * {@code "+64 21 123 456"} -> {@code "+6421123456"}.
 */
public final class Telephones {

    private Telephones() {
    }

    /**
     * Converts a telephone number to its E.164 form, or returns {@code null} when the
     * input cannot form a valid E.164 number.
     */
    public static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        return digits.matches("\\d{8,15}") ? "+" + digits : null;
    }
}
