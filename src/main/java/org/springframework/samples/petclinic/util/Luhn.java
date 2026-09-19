package org.springframework.samples.petclinic.util;

/**
 * Computes the Luhn check digit over the decimal digits contained in a string. Non-digit
 * characters are ignored, so a formatted code such as a customer code ('SYD-SMI-0007') is
 * reduced to its digits before the standard Luhn algorithm is applied: working right to
 * left, every second digit is doubled (subtracting 9 when the result exceeds 9), the digits
 * are summed and the check digit is the amount needed to round the sum up to a multiple of 10.
 */
public final class Luhn {

    private Luhn() {
    }

    /** The Luhn check digit (0-9) over the decimal digits contained in {@code s}. */
    public static int checkDigit(String s) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}
