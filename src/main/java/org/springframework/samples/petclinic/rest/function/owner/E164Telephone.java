package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Converts a raw telephone string to canonical E.164 form.
 *
 * <p>Rules:
 * <ul>
 *   <li>strip spaces, dashes and brackets;</li>
 *   <li>keep a leading {@code '+'} and country code when present;</li>
 *   <li>otherwise assume country code {@code '+61'} and drop a single leading {@code '0'}
 *       from the national digits;</li>
 *   <li>require 8 to 15 digits after the {@code '+'}.</li>
 * </ul>
 *
 * <p>So {@code "0412 345 678"} becomes {@code "+61412345678"} and {@code "+64 21 123 456"}
 * becomes {@code "+6421123456"}. A value that cannot form valid E.164 has no canonical form.
 */
public final class E164Telephone {

    private E164Telephone() {
    }

    /**
     * @return the E.164 form of {@code raw}, or {@code null} if it cannot form a valid E.164 number.
     */
    public static String toE164OrNull(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1); // drop a single leading national-trunk '0'
            }
            digits = "61" + cleaned;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
