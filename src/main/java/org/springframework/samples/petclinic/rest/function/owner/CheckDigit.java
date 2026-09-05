package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Computes the Luhn {@code checkDigit} (0-9) over the digits contained in an owner's
 * {@code customerCode}. Non-digit characters (the separating dashes) are ignored, so the
 * check digit is taken over the CITY3/LAST3/NNNN digits alone. Derived deterministically
 * from the customer code, so it is stable across requests and needs no persisted column.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /** The Luhn check digit over the digits of {@code s}, or 0 when {@code s} is null. */
    public static int of(String s) {
        if (s == null) {
            return 0;
        }
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
