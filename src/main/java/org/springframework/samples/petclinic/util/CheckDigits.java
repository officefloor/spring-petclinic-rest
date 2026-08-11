package org.springframework.samples.petclinic.util;

/**
 * Computes a single Luhn check digit (0-9) over the decimal digits contained in a string — the CHK
 * segment of an owner's {@code memberId}, computed over the digits of {@code '<REGION><FY><HASH8>'}.
 *
 * <p>Non-digit characters (such as letters in the region or hash) are ignored; the remaining digits
 * are processed right-to-left, doubling every second digit starting with the rightmost and casting
 * out nines, and the check digit is {@code (10 - (sum % 10)) % 10}.
 */
public final class CheckDigits {

    private CheckDigits() {
    }

    /** Luhn check digit (0-9) over the decimal digits contained in {@code s}. */
    public static int luhn(String s) {
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
