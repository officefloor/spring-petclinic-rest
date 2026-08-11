package org.springframework.samples.petclinic.mapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Formats an owner's stored E.164 {@code telephone} into the human-readable {@code telephoneDisplay}
 * &mdash; the country code, a space, then the national digits grouped in threes, so
 * {@code "+61412345678"} becomes {@code "+61 412 345 678"}. The raw {@code telephone} stays E.164.
 *
 * <p>The country code is split off using the same known-country table as the E.164 normaliser
 * ({@code +61} with 9 national digits, {@code +1} with 10), matched longest-prefix first; an
 * unrecognised country code falls back to its first two digits so a space still separates the
 * country code from the national digits.
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does not mistake
 * it for an implicit {@code String -> String} mapping method and apply it to every string property.
 */
public final class TelephoneDisplay {

    /**
     * Country code (without the {@code '+'}) to its national-number length. Ordered longest-prefix
     * first so a {@code +61} number is matched by {@code "61"} before {@code "1"}.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH.put("61", 9);
        NATIONAL_LENGTH.put("1", 10);
    }

    private TelephoneDisplay() {
    }

    /**
     * The human-readable form of the E.164 {@code telephone}. Returns the input unchanged when it is
     * {@code null} or not an E.164 number (a {@code '+'} followed by 8 to 15 digits).
     */
    public static String of(String telephone) {
        if (telephone == null || !telephone.matches("\\+\\d{8,15}")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String code = null;
        for (Map.Entry<String, Integer> country : NATIONAL_LENGTH.entrySet()) {
            if (digits.startsWith(country.getKey())
                    && digits.length() - country.getKey().length() == country.getValue()) {
                code = country.getKey();
                break;
            }
        }
        if (code == null) {
            // Unrecognised country code: keep a leading two-digit code so a space still separates it.
            code = digits.substring(0, Math.min(2, digits.length()));
        }
        return "+" + code + " " + group(digits.substring(code.length()));
    }

    /** Group the national digits into space-separated runs of three, left to right. */
    private static String group(String national) {
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
