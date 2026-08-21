package org.springframework.samples.petclinic.util;

/**
 * Computes the Luhn check digit over the digits contained in a string. Non-digit characters are
 * ignored, so the customer code separators ({@code -}) do not contribute. This is the standard
 * Luhn algorithm: doubling every second digit from the right and reducing any result above nine
 * by nine, then returning the digit that makes the total a multiple of ten.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /**
     * @param value the string whose contained digits to check.
     * @return the Luhn check digit (0-9) over the digits in {@code value}.
     */
    public static int luhn(String value) {
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
