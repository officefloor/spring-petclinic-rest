package org.springframework.samples.petclinic.mapper;

/**
 * Formats a stored E.164 telephone for humans: the country code, a space, then the
 * national digits grouped in threes (e.g. {@code +61412345678} => {@code +61 412 345
 * 678}). The country code mirrors the numbers produced when owners are created ('+1',
 * otherwise a two-digit code such as '+61'). Non-E.164 values are returned unchanged.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String of(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String countryCode = digits.startsWith("1") ? "1" : digits.substring(0, 2);
        String national = digits.substring(countryCode.length());
        StringBuilder grouped = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i++) {
            grouped.append(i % 3 == 0 ? ' ' : "").append(national.charAt(i));
        }
        return grouped.toString();
    }
}
