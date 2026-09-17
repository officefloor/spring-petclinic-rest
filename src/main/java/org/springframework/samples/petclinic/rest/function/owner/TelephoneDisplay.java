package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single definition of an owner's human-readable telephone: the stored E.164 number (see
 * {@link E164Telephone}) formatted for display as the country code, a space, then the national
 * digits grouped in threes, e.g. {@code "+61412345678"} -> {@code "+61 412 345 678"}. The raw
 * E.164 value is kept untouched; this is only its display form.
 *
 * <p>Lookup only; a pure function of the stored telephone, so it is computed on read rather
 * than stored. Absent (null) until a telephone is on file. When the leading digits match no
 * recognised country code the whole number is grouped in threes without a split.
 */
public final class TelephoneDisplay {

    /** Number of national digits per space-separated group. */
    private static final int GROUP_SIZE = 3;

    private TelephoneDisplay() {
    }

    /** The display form of the E.164 {@code telephone}, or {@code null} when it is absent. */
    public static String of(String telephone) {
        if (telephone == null || telephone.isBlank()) {
            return null;
        }
        String digits = telephone.startsWith("+") ? telephone.substring(1) : telephone;
        String countryCode = E164Telephone.countryCode(telephone);
        if (countryCode == null) {
            return "+" + group(digits);
        }
        String national = digits.substring(countryCode.length());
        return "+" + countryCode + " " + group(national);
    }

    /** Group {@code digits} into space-separated runs of {@link #GROUP_SIZE}, left to right. */
    private static String group(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % GROUP_SIZE == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }
}
