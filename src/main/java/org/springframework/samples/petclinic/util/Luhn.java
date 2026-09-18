package org.springframework.samples.petclinic.util;

/**
 * Luhn check-digit computation over the digits contained in a string. Non-digit
 * characters are ignored, so it works directly on formatted codes such as a
 * customer code ('PAR-FRA-0007').
 */
public final class Luhn {

    private Luhn() {
    }

    /**
     * The Luhn check digit (0-9) computed over the digit characters of {@code s},
     * scanning right-to-left and doubling every other digit starting with the last.
     */
    public static int checkDigit(String s) {
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
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
