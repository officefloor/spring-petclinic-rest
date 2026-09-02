package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Formats a stored E.164 telephone for humans: the country code, a space, then the national digits
 * grouped in threes (e.g. {@code "+61412345678"} → {@code "+61 412 345 678"}). Returns the input
 * unchanged when it is not in E.164 form.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String of(String e164) {
        if (e164 == null || e164.length() < 2 || e164.charAt(0) != '+') {
            return e164;
        }
        String digits = e164.substring(1);
        char first = digits.charAt(0);
        int ccLen = (first == '1' || first == '7') ? 1 : 2;
        String cc = digits.substring(0, ccLen);
        String national = digits.substring(ccLen);
        StringBuilder grouped = new StringBuilder("+").append(cc);
        for (int i = 0; i < national.length(); i++) {
            grouped.append(i % 3 == 0 ? ' ' : "").append(national.charAt(i));
        }
        return grouped.toString();
    }
}
