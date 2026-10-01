package org.springframework.samples.petclinic.util;

/**
 * Computes a single Luhn check digit over the digits contained in a string.
 *
 * <p>The standard Luhn algorithm scans the input right-to-left over its digit
 * characters (any non-digit is ignored), doubling every second digit and subtracting
 * 9 from any doubled value greater than 9, then sums the results. The check digit is
 * the amount that would bring that sum to the next multiple of ten, i.e.
 * {@code (10 - (sum % 10)) % 10}, always a value from 0 to 9.
 */
public final class LuhnCheckDigit {

    private LuhnCheckDigit() {
    }

    /**
     * Compute the Luhn check digit over the digits contained in {@code s}.
     *
     * @param s the source string (only its digit characters are considered)
     * @return the Luhn check digit, a value from 0 to 9
     */
    public static int compute(String s) {
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
