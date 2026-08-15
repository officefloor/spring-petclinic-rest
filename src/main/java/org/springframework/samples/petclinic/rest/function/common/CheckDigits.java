package org.springframework.samples.petclinic.rest.function.common;

/**
 * Derives a single Luhn check digit over the digits contained in a value.
 *
 * <p>Used for the owner's {@code checkDigit}, computed over the digits of its
 * {@code customerCode}. Non-digit characters are ignored, so the region prefix and separator in a
 * code such as {@code 'NSW-1A2B3C4D'} do not contribute; the rightmost digit is doubled first per the
 * standard Luhn algorithm.
 */
public final class CheckDigits {

    private CheckDigits() {
    }

    /** The Luhn check digit (0-9) over the digits contained in {@code value}; 0 when null. */
    public static int luhnOf(String value) {
        if (value == null) {
            return 0;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
