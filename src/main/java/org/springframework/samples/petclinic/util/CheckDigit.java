package org.springframework.samples.petclinic.util;

/**
 * Derives an owner's {@code checkDigit}: a single Luhn check digit (0-9) computed over the
 * digits contained in the owner's {@code customerCode}. Non-digit characters (the separator and
 * letters of a code such as {@code NSW-3F1A9C2B}) are skipped, and the standard Luhn algorithm is
 * applied to the remaining digits, doubling every second digit from the right.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /**
     * Returns the Luhn check digit (0-9) over the digits contained in {@code code}, or
     * {@code null} when {@code code} is {@code null}.
     */
    public static Integer luhnOf(String code) {
        if (code == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
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
