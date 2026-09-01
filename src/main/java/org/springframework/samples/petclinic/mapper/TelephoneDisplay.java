package org.springframework.samples.petclinic.mapper;

/** Formats a stored E.164 telephone for humans: the country code, a space, then the
 * national digits grouped in threes (e.g. "+61412345678" -> "+61 412 345 678"). */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String of(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        int ccLen = Math.min(e164.startsWith("+1") ? 1 : 2, digits.length());
        StringBuilder out = new StringBuilder("+").append(digits, 0, ccLen);
        for (int i = ccLen; i < digits.length(); i += 3) {
            out.append(' ').append(digits, i, Math.min(i + 3, digits.length()));
        }
        return out.toString();
    }
}
