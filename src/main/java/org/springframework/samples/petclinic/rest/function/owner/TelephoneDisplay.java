package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Formats a stored E.164 telephone number for humans: the country code, a space, then the national
 * digits grouped in threes (e.g. {@code '+61412345678'} becomes {@code '+61 412 345 678'}). The raw
 * {@code telephone} field keeps its E.164 form; this is only the display rendering.
 *
 * <p>The country code is recognised from the same set the app normalizes to ({@code '+61'} and
 * {@code '+1'}); anything else keeps its full digit run grouped in threes after the {@code '+'}.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    /**
     * Renders {@code e164} for display, or returns it unchanged when it is null or not E.164 form.
     */
    public static String of(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        if (!digits.matches("\\d+")) {
            return e164;
        }
        int ccLength;
        if (digits.startsWith("61")) {
            ccLength = 2;
        }
        else if (digits.startsWith("1")) {
            ccLength = 1;
        }
        else {
            ccLength = 0;
        }
        String countryCode = digits.substring(0, ccLength);
        String national = digits.substring(ccLength);
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }
}
