package org.springframework.samples.petclinic.model;

/**
 * The single definition of the <em>Luhn check digit</em> computed over the digits of a
 * customer code. Non-digit characters (separators, letters) are ignored; the remaining
 * digits are folded right-to-left with the rightmost digit doubled, and the check digit is
 * the amount that rounds the running sum up to the next multiple of ten.
 *
 * <p>Returned by the response mapper alongside the customer code so a caller can verify the
 * code was transcribed correctly.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /** The Luhn check digit (0-9) over the digit characters of {@code code}. */
    public static int luhn(String code) {
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
