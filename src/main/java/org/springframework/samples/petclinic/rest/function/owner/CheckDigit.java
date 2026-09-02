package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Computes the {@code checkDigit}: a single Luhn check digit over the digits contained in a
 * customer code. Non-digit characters (such as the '-' separators) are skipped.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /** The Luhn check digit (0-9) over the digits in {@code code}. */
    public static int of(String code) {
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl && (d *= 2) > 9) {
                d -= 9;
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
