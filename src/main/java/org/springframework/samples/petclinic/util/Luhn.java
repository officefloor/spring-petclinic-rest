package org.springframework.samples.petclinic.util;

/**
 * Computes the Luhn check digit over the digits contained in a string. Non-digit
 * characters are ignored, so a formatted code such as {@code LON-SMI-0007} is checked on
 * its digits alone. The rightmost digit is doubled first, each doubled value above 9 has 9
 * subtracted, and the check digit is the amount that rounds the running total up to the
 * next multiple of ten.
 */
public final class Luhn {

    private Luhn() {
    }

    /** The Luhn check digit (0-9) over the digits contained in {@code value}. */
    public static int checkDigit(String value) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
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
