package org.springframework.samples.petclinic.model;

/**
 * Computes a single Luhn check digit (0-9) over the digits contained in a string — the one
 * place the algorithm lives, so the several places that pin a Luhn digit onto an identifier
 * (such as an owner's {@link CheckDigit}) delegate here rather than re-implementing it.
 *
 * <p>The digits are processed right-to-left, doubling every second digit starting with the
 * rightmost (subtracting 9 when a doubled digit exceeds 9); the check digit is the amount
 * needed to bring the running total to the next multiple of ten. Non-digit characters are
 * ignored.
 */
public final class Luhn {

    private Luhn() {
    }

    /** The Luhn check digit (0-9) over the digits contained in {@code source}. */
    public static int digit(String source) {
        int sum = 0;
        boolean dbl = true;
        for (int i = source.length() - 1; i >= 0; i--) {
            char c = source.charAt(i);
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
