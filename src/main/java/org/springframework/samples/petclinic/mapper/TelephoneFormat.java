package org.springframework.samples.petclinic.mapper;

/**
 * Formats a stored E.164 telephone number for human display. The raw {@code telephone}
 * field is kept in strict E.164 form; the derived {@code telephoneDisplay} presents the
 * same number as the country code, a space, then the national digits grouped in threes
 * from the left (e.g. {@code +61412345678} becomes {@code +61 412 345 678}).
 *
 * <p>Kept out of {@link OwnerMapper} as a static-method holder so MapStruct does not
 * mistake it for a {@code String -> String} mapping method and apply it to every string
 * property, mirroring {@link OwnerLocality}.
 */
public final class TelephoneFormat {

    private TelephoneFormat() {
    }

    /**
     * Format a stored E.164 telephone for display. The country code is split off (Australia
     * {@code +61} and NANP {@code +1} are recognised, matching the create-time normalization),
     * followed by a space and the national digits grouped in threes. A value that is null or
     * not a {@code '+'}-prefixed digit string is returned unchanged.
     *
     * @param telephone the stored E.164 telephone (e.g. {@code +61412345678})
     * @return the human-readable form (e.g. {@code +61 412 345 678}), or the input unchanged
     *         when it is not a formattable E.164 number
     */
    public static String display(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (!digits.matches("[0-9]+")) {
            return telephone;
        }
        String countryCode;
        if (digits.startsWith("61")) {
            countryCode = "61";
        }
        else if (digits.startsWith("1")) {
            countryCode = "1";
        }
        else {
            countryCode = "";
        }
        String national = digits.substring(countryCode.length());
        String grouped = groupInThrees(national);
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        if (!countryCode.isEmpty() && !grouped.isEmpty()) {
            sb.append(' ');
        }
        return sb.append(grouped).toString();
    }

    /** Group the digits into space-separated blocks of three, from the left. */
    private static String groupInThrees(String national) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(national.charAt(i));
        }
        return sb.toString();
    }
}
