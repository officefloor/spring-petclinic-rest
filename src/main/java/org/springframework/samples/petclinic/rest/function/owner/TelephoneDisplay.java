package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Formats a stored E.164 telephone (e.g. {@code +61412345678}) for humans: the country
 * code, a space, then the national digits grouped in threes (e.g.
 * {@code +61 412 345 678}). The country code is the leading '61' when present, otherwise
 * a single leading digit - matching the numbers {@link E164Telephone} produces. Returns
 * {@code null} when the input is not an E.164 number.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String of(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return null;
        }
        String digits = e164.substring(1);
        int countryCodeLength = digits.startsWith("61") ? 2 : 1;
        String national = digits.substring(countryCodeLength);
        return "+" + digits.substring(0, countryCodeLength) + " "
                + national.replaceAll("(\\d{3})(?=\\d)", "$1 ");
    }
}
