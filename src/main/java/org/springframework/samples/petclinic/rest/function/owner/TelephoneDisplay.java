package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Formats a stored E.164 telephone for humans: the country code, a space, then the national
 * digits grouped in threes from the left (e.g. {@code +61412345678} -> {@code +61 412 345 678}).
 * The raw {@code telephone} stays in E.164 form; this only derives the {@code telephoneDisplay}
 * response field. The known country codes match {@link NormalizeOwnerTelephone} ({@code +61} and
 * {@code +1}); any other code is treated as two digits.
 *
 * <p>Kept as a standalone class (not a mapper default method) so MapStruct does not mistake it for
 * a {@code String -> String} property-conversion method and apply it while mapping the entity.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    /**
     * Returns the human-formatted display of the given E.164 telephone, or the input unchanged
     * when it is null or not a {@code +}-prefixed digit string.
     */
    public static String of(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (!digits.matches("\\d+")) {
            return telephone;
        }
        String countryCode;
        if (digits.startsWith("61")) {
            countryCode = "61";
        } else if (digits.startsWith("1")) {
            countryCode = "1";
        } else {
            countryCode = digits.length() > 2 ? digits.substring(0, 2) : digits;
        }
        String national = digits.substring(countryCode.length());
        StringBuilder display = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
    }
}
