package org.springframework.samples.petclinic.mapper;

/**
 * Computes an owner's {@code checkDigit} &mdash; a single Luhn check digit (0-9) over the digits
 * contained in the owner's {@code customerCode}.
 *
 * <p>Non-digit characters (the region letters and the separator in {@code '<REGION>-<HASH8>'}, and
 * the {@code A-F} hex letters of the hash) are ignored. The digits are processed right-to-left,
 * doubling every second digit starting with the rightmost;
 * a doubled value above nine has nine subtracted. The check digit is the amount that brings the
 * running sum up to the next multiple of ten.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /** Luhn check digit (0-9) over the digits contained in {@code s}. */
    public static int of(String s) {
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
