package org.springframework.samples.petclinic.util;

/**
 * The Luhn (mod 10) algorithm. Computes the single check digit that, appended to a sequence
 * of digits, makes the whole sequence pass the Luhn checksum.
 */
public final class Luhn {

    private Luhn() {
    }

    /**
     * The Luhn check digit (0-9) over the digits contained in {@code value}; non-digit
     * characters are ignored.
     */
    public static int checkDigit(String value) {
        int sum = 0;
        boolean doubling = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubling) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubling = !doubling;
        }
        return (10 - (sum % 10)) % 10;
    }
}
