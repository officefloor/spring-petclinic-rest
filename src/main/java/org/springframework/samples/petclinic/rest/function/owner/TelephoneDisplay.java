package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Human-readable rendering of a stored E.164 telephone: the country code, a space, then the national
 * digits grouped in threes (e.g. {@code +61412345678} -> {@code +61 412 345 678}). The raw
 * {@code telephone} stays E.164; this only affects the derived {@code telephoneDisplay}. The country
 * code is split using the same codes {@link BuildOwner} recognises ({@code +61}, {@code +1}),
 * defaulting to a two-digit code otherwise.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String of(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        int ccLen = digits.startsWith("1") && !digits.startsWith("61") ? 1 : 2;
        String national = digits.substring(Math.min(ccLen, digits.length()));
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + digits.substring(0, Math.min(ccLen, digits.length()))
                + (grouped.length() == 0 ? "" : " " + grouped);
    }
}
