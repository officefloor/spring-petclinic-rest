package org.springframework.samples.petclinic.util;

/**
 * Computes the Luhn check digit over the digits contained in a string.
 */
public final class Luhn {

    private Luhn() {
    }

    /** The Luhn check digit (0-9) over the digits contained in {@code s}. */
    public static int checkDigit(String s) {
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl && (d *= 2) > 9) {
                d -= 9;
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
