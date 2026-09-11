package org.springframework.samples.petclinic.mapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Formats an owner's stored E.164 telephone into a human-friendly display string. The raw {@code telephone} is kept in
 * canonical E.164 form ({@code +61412345678}); this derives {@code telephoneDisplay} by separating the country code
 * from the national number with a space and grouping the national digits in threes, e.g. {@code +61 412 345 678}.
 *
 * <p>Known country codes (longest first, mirroring the telephone normalizer) let the country code boundary be found
 * exactly; an unrecognized code falls back to grouping every digit after the {@code '+'} in threes. Kept out of
 * {@link OwnerMapper} so MapStruct does not mistake it for an implicit {@code String -> String} property mapping method.
 */
public final class TelephoneDisplayResolver {

    /** Known country codes, ordered longest first so the most specific prefix wins. */
    private static final Map<String, Integer> NATIONAL_DIGITS_BY_COUNTRY_CODE = buildNationalDigitsByCountryCode();

    private static Map<String, Integer> buildNationalDigitsByCountryCode() {
        Map<String, Integer> byLongestFirst = new LinkedHashMap<>();
        byLongestFirst.put("61", 9);
        byLongestFirst.put("1", 10);
        return byLongestFirst;
    }

    private TelephoneDisplayResolver() {
    }

    /**
     * Formats the given stored E.164 telephone for humans: {@code '+' + countryCode + ' ' + nationalDigitsGroupedInThrees}.
     * For example {@code +61412345678} becomes {@code +61 412 345 678}. Returns the input unchanged when it is
     * {@code null}, blank, or not in {@code '+'}-prefixed E.164 form.
     *
     * @param telephone the owner's stored E.164 telephone, may be {@code null}
     * @return the human-friendly display form of the telephone
     */
    public static String deriveTelephoneDisplay(String telephone) {
        if (telephone == null || telephone.isBlank() || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1).replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return telephone;
        }
        String countryCode = detectCountryCode(digits);
        String national = digits.substring(countryCode.length());
        String grouped = groupInThrees(national);
        return "+" + countryCode + (grouped.isEmpty() ? "" : " " + grouped);
    }

    /**
     * Returns the recognized country code that prefixes the digits, or an empty string when none is known so the
     * caller groups every digit as the national number.
     */
    private static String detectCountryCode(String digits) {
        for (String countryCode : NATIONAL_DIGITS_BY_COUNTRY_CODE.keySet()) {
            if (digits.startsWith(countryCode)) {
                return countryCode;
            }
        }
        return "";
    }

    /** Groups the digits left-to-right into space-separated blocks of three (the final block may be shorter). */
    private static String groupInThrees(String digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }
}
