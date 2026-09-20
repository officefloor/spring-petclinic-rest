package org.springframework.samples.petclinic.model;

/**
 * The Luhn check-digit algorithm. Non-digit characters are ignored, so the digit is
 * computed over the digits contained in the given string.
 */
public final class Luhn {

    private Luhn() {
    }

    /**
     * The Luhn check digit (0-9) over the digits contained in {@code value}, computed by
     * doubling every second digit from the right (subtracting 9 when the result exceeds 9),
     * summing, and taking the amount needed to reach the next multiple of ten.
     */
    public static int checkDigit(String value) {
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
