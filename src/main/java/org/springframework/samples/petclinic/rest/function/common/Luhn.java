package org.springframework.samples.petclinic.rest.function.common;

/**
 * Computes the Luhn check digit over the digits contained in a string. Non-digit
 * characters are ignored, so a value such as {@code SYD-SMI-0007} is treated as its
 * digit sequence {@code 0007}. Doubling starts from the rightmost digit, matching the
 * standard Luhn convention for a bare (un-appended) payload.
 */
public final class Luhn {

    private Luhn() {
    }

    /**
     * The Luhn check digit (0-9) over the digits contained in {@code s}, or {@code 0}
     * when {@code s} is null or holds no digits.
     */
    public static int checkDigit(String s) {
        if (s == null) {
            return 0;
        }
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
