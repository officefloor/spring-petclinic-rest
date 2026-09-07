package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Formats a stored E.164 telephone for human display: the country code, a space, then the national
 * digits grouped in threes. The raw stored value stays untouched in E.164 form.
 *
 * <p>The country code is split off the same way {@link OwnerTelephone} treats it: {@code '+1'} is a
 * one-digit code and every other supported code (including the default {@code '+61'}) is two digits.
 * The national digits are then grouped left-to-right in threes.
 *
 * <p>Examples: {@code "+61412345678" -> "+61 412 345 678"}, {@code "+6421123456" -> "+64 211 234 56"}.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    /**
     * @return the E.164 telephone formatted for humans, or the input unchanged when it is {@code null}
     *         or not a {@code '+'}-prefixed run of digits.
     */
    public static String of(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        if (!digits.matches("[0-9]+")) {
            return e164;
        }
        int countryCodeLength = digits.startsWith("1") ? 1 : 2;
        if (digits.length() <= countryCodeLength) {
            return e164;
        }
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder grouped = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i++) {
            grouped.append(i % 3 == 0 ? ' ' : "").append(national.charAt(i));
        }
        return grouped.toString();
    }
}
