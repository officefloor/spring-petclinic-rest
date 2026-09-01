package org.springframework.samples.petclinic.mapper;

/**
 * Formats a stored E.164 telephone number for humans: the country code, a
 * space, then the national digits grouped in threes (e.g. "+61412345678"
 * becomes "+61 412 345 678"). Supports the country codes used by owners,
 * '+61' (9 national digits) and '+1' (10 national digits).
 */
final class TelephoneDisplays {

    private TelephoneDisplays() {
    }

    /** Human-readable form of the E.164 {@code number}, or {@code null} for {@code null}. */
    static String format(String number) {
        if (number == null) {
            return null;
        }
        String digits = number.substring(1);
        String countryCode = digits.startsWith("61") ? "61" : "1";
        String national = digits.substring(countryCode.length());
        StringBuilder result = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i++) {
            if (i % 3 == 0) {
                result.append(' ');
            }
            result.append(national.charAt(i));
        }
        return result.toString();
    }
}
